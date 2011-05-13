/*
 * Copyright (C) 2010 Andrea Dalle Vacche.
 * 
 * This file is part of orabbix.
 *
 * orabbix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * orabbix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * orabbix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartmarmot.orabbix;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.smartmarmot.common.SmartLogger;
import com.smartmarmot.common.db.DBConn;

public class Configurator {
	private static Properties _props;

	protected static Properties getPropsFromFile(String _filename) {
		try {
			verifyConfig();
			Properties propsq = new Properties();
			FileInputStream fisq;
			if (_filename != "" && _filename != null) {
				File queryFile = new File(_filename);
				fisq = new FileInputStream(new java.io.File(queryFile
						.getAbsoluteFile().getCanonicalPath()));
				queryFile = null;
				SmartLogger.logThis(Level.DEBUG, "Loaded the properties from " + _filename);
				propsq.load(fisq);
				fisq.close();
				return propsq;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.ERROR, "Error on Configurator reading properties file getPropsFromFile("
					+ _filename + ") " + e.getMessage());
			return null;
		}
		return null;
	}

	protected static Query[] getQueries(Properties _propsq) throws Exception {
		return getQueries(Constants.QUERY_LIST, _propsq);
	}

	
	private static Query[] getQueries(String parameter, Properties _propsq)
			throws Exception {
		try {
			StringTokenizer stq = new StringTokenizer(_propsq
					.getProperty(parameter), Constants.DELIMITER);
			String[] QueryLists = new String[stq.countTokens()];
			int count = 0;
			while (stq.hasMoreTokens()) {
				String token = stq.nextToken().toString().trim();
				QueryLists[count] = token;
				count++;
			}

			Collection<Query> Queries = new ArrayList<Query>();
			for (int i = 0; i < QueryLists.length; i++) {
				Query q = getQueryProperties(_propsq, QueryLists[i]);
				Queries.add(q);
			}
			Query[] queries = (Query[]) Queries.toArray(new Query[0]);
			return queries;
		} catch (Exception ex) {

			SmartLogger.logThis(Level.ERROR, "Error on Configurator on reading properties file "+ _propsq.toString() +" getQueries("
					+ parameter + "," + _propsq.toString() + ") "
					+ ex.getMessage());
			return null;
		}

	}

	private static Query getQueryProperties(Properties _propsq,
			String _queryName) throws Exception {
		try {

			String query = "";
			try {
				query = new String(_propsq.getProperty(_queryName + "."
						+ Constants.QUERY_POSTFIX));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.ERROR, "Error while getting " + _queryName + "."
						+ Constants.QUERY_POSTFIX + " " + ex.getMessage());
			}

			String noDataFound = "";
			try {
				noDataFound = new String(_propsq.getProperty(_queryName + "."
						+ Constants.QUERY_NO_DATA_FOUND));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName
						+ "." + Constants.QUERY_NO_DATA_FOUND
						+ " null or not present " + ex.getMessage());
			}
			String raceCondQuery = "";
			try {
				raceCondQuery = new String(_propsq.getProperty(_queryName + "."
						+ Constants.RACE_CONDITION_QUERY));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName
						+ "." + Constants.RACE_CONDITION_QUERY
						+ " null or not present " + ex.getMessage());
			}
			String raceCondValue = "";
			try {
				raceCondValue = new String(_propsq.getProperty(_queryName + "."
						+ Constants.RACE_CONDITION_VALUE));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName
						+ "." + Constants.RACE_CONDITION_VALUE
						+ " null or not present " + ex.getMessage());
			}
			/**
			 * set Period if not defined period =5 min.
			 */
			int period = -1;
			try {
				period = new Integer(_propsq.getProperty(_queryName + "."
						+ Constants.QUERY_PERIOD));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName
						+ "." + Constants.QUERY_PERIOD
						+ " null or not present " + ex.getMessage());
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.QUERY_PERIOD
						+ " null or not present using default values 5 min.");
				period = 5;

			}
			Boolean active = true;
			try {
				String active_str = _propsq.getProperty(_queryName + "."
						+ Constants.QUERY_ACTIVE);
				if (active_str != null) {
					if (active_str.equalsIgnoreCase("false")) {
						active = false;
					}
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName
						+ "." + Constants.QUERY_ACTIVE
						+ " null or not present " + ex.getMessage());
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.QUERY_ACTIVE
						+ " null or not present using default values TRUE");
			}

			Boolean trim = true;
			try {
				String trim_str = _propsq.getProperty(_queryName + "."
						+ Constants.QUERY_TRIM);
				if (trim_str != null) {
					if (trim_str.equalsIgnoreCase("false")) {
						trim = false;
					}
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.QUERY_TRIM + " null or not present "
						+ ex.getMessage());
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.QUERY_TRIM
						+ " null or not present using default values TRUE");
			}

			Boolean space = false;
			try {
				String space_str = _propsq.getProperty(_queryName + "."
						+ Constants.QUERY_SPACE);
				if (space_str != null) {
					if (space_str.equalsIgnoreCase("false")) {
						space = false;
					}
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.QUERY_SPACE + " null or not present "
						+ ex.getMessage());
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.QUERY_SPACE
						+ " null or not present using default values TRUE");
			}

			List<Integer> excludeColumns = new ArrayList<Integer>();
			try {
				String excludeColumnsList = new String(_propsq
						.getProperty(_queryName + "."
								+ Constants.QUERY_EXCLUDE_COLUMNS));

				StringTokenizer st = new StringTokenizer(excludeColumnsList,
						Constants.DELIMITER);
				while (st.hasMoreTokens()) {
					String token = st.nextToken().toString();
					Integer tmpInteger = new Integer(token);
					excludeColumns.add(tmpInteger);
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.QUERY_EXCLUDE_COLUMNS + " error "
						+ ex.getMessage());
			}

			List<Integer> raceExcludeColumns = new ArrayList<Integer>();
			try {
				String excludeColumnsList = new String(_propsq
						.getProperty(_queryName + "."
								+ Constants.RACE_CONDITION_EXCLUDE_COLUMNS));

				StringTokenizer st = new StringTokenizer(excludeColumnsList,
						Constants.DELIMITER);
				while (st.hasMoreTokens()) {
					String token = st.nextToken().toString();
					Integer tmpInteger = new Integer(token);
					excludeColumns.add(tmpInteger);
				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + _queryName + "."
						+ Constants.RACE_CONDITION_EXCLUDE_COLUMNS + " error "
						+ ex.getMessage());
			}
			
			
			Query q = new Query(query, _queryName, noDataFound, raceCondQuery,
					raceCondValue, period, active, trim, space, excludeColumns,
					raceExcludeColumns);

			return q;
		} catch (Exception ex) {

			SmartLogger.logThis(Level.ERROR, "Error on Configurator on getQueryProperties("
					+ _propsq.toString() + ") " + ex.getMessage());
			return null;
		}
	}


	protected static Query[] refresh(Query[] _myquery, String _prpFile) {
		Properties _prp = getPropsFromFile(_prpFile);
		for (int i = 0; i < _myquery.length; i++) {
			try {
				Query qnew = Configurator.getQueryProperties(_prp, _myquery[i]
						.getName());
				if (!_myquery[i].getSQL().equalsIgnoreCase(qnew.getSQL())) {
					_myquery[i].setSql(qnew.getSQL());
					SmartLogger.logThis(Level.INFO, "Statement changed on query "
							+ _myquery[i].getName() + " new statement ->"
							+ qnew.getSQL());
				}
				if ((_myquery[i].getActive().equals(true) && qnew.getActive()
						.equals(false))
						|| (_myquery[i].getActive().equals(false) && qnew
								.getActive().equals(true))) {

					_myquery[i].setActive(qnew.getActive());
					SmartLogger.logThis(Level.INFO, "Status of query "
							+ _myquery[i].getName() + " has changed ->"
							+ qnew.getActive());
				}
				if (_myquery[i].getPeriod() != qnew.getPeriod()) {
					_myquery[i].setPeriod(qnew.getPeriod());
					if (qnew.getPeriod() > 0) {
						Date now = new Date(System.currentTimeMillis());
						Date newNextRun = new Date(now.getTime()
								+ (qnew.getPeriod() * 1000 * 60));
						_myquery[i].setNextrun(newNextRun);
					}
					SmartLogger.logThis(Level.INFO, "Period of query "
							+ _myquery[i].getName() + " has changed ->"
							+ qnew.getPeriod());
				}
				if (!_myquery[i].getRaceQuery().equalsIgnoreCase(
						qnew.getRaceQuery())) {
					_myquery[i].setRaceQuery(qnew.getRaceQuery());
					SmartLogger.logThis(Level.INFO, "RaceQuery for query "
							+ _myquery[i].getName() + " has changed ->"
							+ qnew.getRaceQuery());
				}
				if (!_myquery[i].getRaceValue().equalsIgnoreCase(
						qnew.getRaceValue())) {
					_myquery[i].setRaceQuery(qnew.getRaceQuery());
					SmartLogger.logThis(Level.INFO, "RaceValue of query "
							+ _myquery[i].getName() + " has changed ->"
							+ qnew.getRaceValue());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				SmartLogger.logThis(Level.ERROR,
						"Error on Configurator while getting getQueryProperties( "
								+ _prp + " , " + _myquery[i].getName()
								+ ") Error " + e.getMessage());
			}
		}
		return _myquery;
	}

	private static void verifyConfig() {
		if (_props == null ) {
			throw new IllegalArgumentException("empty properties");
		}

	}

	public Configurator(String _url) throws IOException {
		Properties props = new Properties();
		
		FileInputStream fis;
		try {
			try {
				File configFile = new File(_url);
				fis = new FileInputStream(new java.io.File(configFile
						.getAbsoluteFile().getCanonicalPath()));
				props.load(fis);
				fis.close();
			} catch (Exception e) {
				SmartLogger.logThis(Level.ERROR,
						"Error on Configurator while retriving configuration file "
								+ _url + " " + e.getMessage());
			}
			_props = props;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.ERROR, "Error on Configurator " + e.getMessage());
		}
	}

	private DBConn getConnection(String dbName) throws Exception {
		try {
			verifyConfig();

			SmartLogger.logThis(Level.DEBUG, "getConnection for database " + dbName);
			String url = "";
			try {
				url = new String(_props.getProperty(dbName + "."
						+ Constants.CONN_URL));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.ERROR,
						"Error on Configurator getConnection while getting "
								+ dbName + "." + Constants.CONN_URL + " "
								+ ex.getMessage());
			}

			String uname = "";
			try {
				uname = new String(_props.getProperty(dbName + "."
						+ Constants.CONN_USERNAME));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.ERROR,
						"Error on Configurator getConnection while getting "
								+ dbName + "." + Constants.CONN_USERNAME + " "
								+ ex.getMessage());
			}
			String password = "";
			try {
				password = new String(_props.getProperty(dbName + "."
						+ Constants.CONN_PASSWORD));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.ERROR,
						"Error on Configurator getConnection while getting "
								+ dbName + "." + Constants.CONN_PASSWORD + " "
								+ ex.getMessage());
			}
			DriverAdapterCPDS cpds = new DriverAdapterCPDS();
			cpds.setDriver(Constants.ORACLE_DRIVER);
			cpds.setUrl(url.toString());
			cpds.setUser(uname.toString());
			cpds.setPassword(password.toString());
			SharedPoolDataSource tds = new SharedPoolDataSource();
			tds.setConnectionPoolDataSource(cpds);
			// tds.setMaxActive(5);
			Integer maxActive = new Integer(5);
			try {
				maxActive = new Integer(_props.getProperty(dbName + "."
						+ Constants.CONN_MAX_ACTIVE));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + dbName + "."
						+ Constants.CONN_MAX_ACTIVE + " " + ex.getMessage());
				try {
					maxActive = new Integer(_props
							.getProperty(Constants.DATABASES_LIST + "."
									+ Constants.CONN_MAX_ACTIVE));
				} catch (Exception e) {
					SmartLogger.logThis(Level.WARN, "Note: "
							+ Constants.DATABASES_LIST + "."
							+ Constants.CONN_MAX_ACTIVE + " " + e.getMessage());
					SmartLogger.logThis(Level.WARN, "Warning I will use default value "
							+ maxActive);
				}
			}
			tds.setMaxActive(maxActive.intValue());
			Integer maxWait = new Integer(100);
			try {
				maxWait = new Integer(_props.getProperty(dbName + "."
						+ Constants.CONN_MAX_WAIT));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + dbName + "."
						+ Constants.CONN_MAX_WAIT + " " + ex.getMessage());
				try {
					maxWait = new Integer(_props
							.getProperty(Constants.DATABASES_LIST + "."
									+ Constants.CONN_MAX_WAIT));
				} catch (Exception e) {
					SmartLogger.logThis(Level.WARN, "Note: "
							+ Constants.DATABASES_LIST + "."
							+ Constants.CONN_MAX_WAIT + " " + e.getMessage());
					SmartLogger.logThis(Level.WARN, "Warning I will use default value "
							+ maxWait);
				}
			}
			tds.setMaxWait(maxWait.intValue());
			Integer maxIdle = new Integer(1);
			try {
				maxIdle = new Integer(_props.getProperty(dbName + "."
						+ Constants.CONN_MAX_IDLE));
			} catch (Exception ex) {
				SmartLogger.logThis(Level.DEBUG, "Note: " + dbName + "."
						+ Constants.CONN_MAX_IDLE + " " + ex.getMessage());
				try {
					maxIdle = new Integer(_props
							.getProperty(Constants.DATABASES_LIST + "."
									+ Constants.CONN_MAX_IDLE));
				} catch (Exception e) {
					SmartLogger.logThis(Level.WARN, "Note: "
							+ Constants.DATABASES_LIST + "."
							+ Constants.CONN_MAX_IDLE + " " + e.getMessage());
					SmartLogger.logThis(Level.WARN, "Warning I will use default value "
							+ maxIdle);
				}
			}
			tds.setMaxIdle(maxIdle.intValue());
			
			SmartLogger.logThis( Level.INFO,"DB Pool created: " + tds);
    		SmartLogger.logThis( Level.INFO, "URL=" + url.toString() );
    		SmartLogger.logThis( Level.INFO, "maxPoolSize=" + tds.getMaxActive() );
    		SmartLogger.logThis( Level.INFO, "maxIdleSize=" + tds.getMaxIdle() );
    		SmartLogger.logThis( Level.INFO, "maxIdleTime=" + tds.getMinEvictableIdleTimeMillis() + "ms" );
    		SmartLogger.logThis( Level.INFO, "poolTimeout=" + tds.getMaxWait() );
    		SmartLogger.logThis( Level.INFO, "timeBetweenEvictionRunsMillis=" + tds.getTimeBetweenEvictionRunsMillis() );
    		SmartLogger.logThis( Level.INFO, "numTestsPerEvictionRun=" + tds.getNumTestsPerEvictionRun() );
			
    		
			tds.setValidationQuery(Constants.ORACLE_VALIDATION_QUERY);
			Connection con = null;
			con = tds.getConnection();
			PreparedStatement p_stmt = null;
			p_stmt = con.prepareStatement(Constants.ORACLE_WHOAMI_QUERY);
			ResultSet rs = null;
			rs = p_stmt.executeQuery();
			String tempStr = new String("");
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			while (rs.next()) {
				for (int r = 1; r < numColumns + 1; r++) {
					tempStr = tempStr + rs.getObject(r).toString().trim();
				}
			}
			SmartLogger.logThis(Level.INFO, "Connected as " + tempStr);

			con.close();
			con = null;
			con = tds.getConnection();
			p_stmt = con.prepareStatement(Constants.ORACLE_DBNAME_QUERY);
			rs = p_stmt.executeQuery();
			rsmd = rs.getMetaData();
			numColumns = rsmd.getColumnCount();
			tempStr = "";
			while (rs.next()) {
				for (int r = 1; r < numColumns + 1; r++) {
					tempStr = tempStr + rs.getObject(r).toString().trim();
				}
			}
			SmartLogger.logThis(Level.INFO, "--------- on Database -> " + tempStr);
			con.close();
			con = null;
			DBConn mydbconn = new DBConn(tds, dbName.toString());
			return mydbconn;

		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR, "Error on Configurator for database " + dbName
					+ " -->" + ex.getMessage());
			return null;
		}
	}

	public DBConn[] getConnections() throws Exception {
		try {
			verifyConfig();
			String[] DatabaseList = getDBList();
			Collection<DBConn> connections = new ArrayList<DBConn>();
			for (int i = 0; i < DatabaseList.length; i++) {
				DBConn dbc=this.getConnection(DatabaseList[i]);
				if (dbc!=null){
					connections.add(dbc);
				}else {
	        		 SmartLogger.logThis(Level.INFO,"This Database "+DatabaseList[i]+" removed");       	
	        	}
			}
			// fis.close();
			DBConn[] connArray = (DBConn[]) connections.toArray(new DBConn[0]);
			return connArray;
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR, "Error on Configurator getConnections "
					+ ex.getMessage());
			return null;
		}
	}

	public String[] getDBList() throws Exception {
		try {
			verifyConfig();
			String dblist = "";
			try {
				dblist = new String(_props
						.getProperty(Constants.DATABASES_LIST));
			} catch (Exception e) {
				SmartLogger.logThis(Level.ERROR,
						"Error on Configurator while retriving the databases list "
								+ Constants.DATABASES_LIST + " " + e);
			}

			StringTokenizer st = new StringTokenizer(dblist,
					Constants.DELIMITER);
			String[] DatabaseList = new String[st.countTokens()];
			int count = 0;
			while (st.hasMoreTokens()) {
				String token = st.nextToken().toString();
				DatabaseList[count] = token;
				count++;
			}
			// fisdb.close();
			return DatabaseList;
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR,
					"Error on Configurator while retriving the databases list "
							+ Constants.DATABASES_LIST + " " + ex);
			return null;
		}
	}

	public Integer getMaxThread() throws Exception {
		try {
			verifyConfig();
			return new Integer(_props
					.getProperty(Constants.ORABBIX_DAEMON_THREAD));
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR, "Error on Configurator while retriving the "
					+ Constants.ORABBIX_DAEMON_THREAD + " " + ex);
			return null;
		}
	}

	public String getPidFile() throws Exception {
		try {
			verifyConfig();
			String _pidfile = new String(_props
					.getProperty(Constants.ORABBIX_PIDFILE));
			SmartLogger.logThis(Level.INFO, "PidFile -> " + _pidfile);

			if (_pidfile == "") {
				SmartLogger.logThis(Level.ERROR, "Error retrieving pidfile from " + _props);
			}
			return _pidfile;

		} catch (Exception ex) {

			SmartLogger.logThis(Level.ERROR, "Error on Configurator getPidFile " + ex);
			return null;
		}
	}

	public String getQueryFile() {
		String queryFile = new String(_props
				.getProperty(Constants.QUERY_LIST_FILE));
		return queryFile;
	}

	public String getQueryFile(String dbName) {
		try {
			verifyConfig();
			if (_props.getProperty(dbName + "." + Constants.QUERY_LIST_FILE) != null) {
				return (_props.getProperty(dbName + "."
						+ Constants.QUERY_LIST_FILE));
			}
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR, "Error on Configurator on getQueryFile("
					+ dbName + ") " + ex.getMessage());
			SmartLogger.logThis(Level.WARN, "I'm going to return getQueryFile() ");
			return getQueryFile();
		}
		return null;
	}

	public Properties getQueryProp() {
		verifyConfig();
		Properties propsq = new Properties();
		try {
			FileInputStream fisq;
			String fiqp = new String(_props
					.getProperty(Constants.QUERY_LIST_FILE));
			File queryFile = new File(fiqp);
			fisq = new FileInputStream(new java.io.File(queryFile
					.getAbsoluteFile().getCanonicalPath()));
			queryFile = null;
			propsq.load(fisq);
			fisq.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.ERROR, "Error on Configurator  while getting "
					+ Constants.QUERY_LIST_FILE + " error -> " + e.getMessage());
			return null;
		}
		return propsq;
	}

	public Properties getQueryProp(String dbname) {
		try {
			verifyConfig();
			Properties propsq = new Properties();
			FileInputStream fisq;
			if (dbname != "" && dbname != null) {
				// logger.warn("Method called with null or empty string on Configurator "+dbname);
				File queryFile = new File(_props.getProperty(dbname + "."
						+ Constants.QUERY_LIST_FILE));
				fisq = new FileInputStream(new java.io.File(queryFile
						.getAbsoluteFile().getCanonicalPath()));
				queryFile = null;
				SmartLogger.logThis(Level.DEBUG, "Debug loaded the " + dbname + "."
						+ Constants.QUERY_LIST_FILE);
				// fisq = new FileInputStream (new java.io.File(
				// _props.getProperty(dbname+"."+Constants.QUERY_LIST_FILE)));
			} else {
				// fisq = new FileInputStream (new java.io.File(
				// _props.getProperty(Constants.QUERY_LIST_FILE)));
				SmartLogger.logThis(Level.DEBUG, "Debug I'm loading the default "
						+ Constants.QUERY_LIST_FILE + " " + dbname
						+ " don't have it's own");
				File queryFile = new File(_props
						.getProperty(Constants.QUERY_LIST_FILE));
				fisq = new FileInputStream(new java.io.File(queryFile
						.getAbsoluteFile().getCanonicalPath()));
				queryFile = null;
			}
			propsq.load(fisq);
			fisq.close();
			return propsq;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.ERROR, "Error on Configurator getting "+Constants.QUERY_LIST_FILE +" " + dbname
					+ e.getMessage());
			return null;
		}
	}

	public String getZabbixServer() throws Exception {
		try {
			verifyConfig();
			return _props.getProperty(Constants.ZABBIX_SERVER_HOST);

		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR,
					"Error on Configurator while retriving zabbix server host "
							+ Constants.ZABBIX_SERVER_HOST + " or port "
							+ Constants.ZABBIX_SERVER_PORT + " " + ex);
			return null;
		}
	}

	public int getZabbixServerPort() throws Exception {
		try {
			verifyConfig();
			Integer port = new Integer(_props
					.getProperty(Constants.ZABBIX_SERVER_PORT));
			return port.intValue();
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR,
					"Error on Configurator while retriving zabbix server port "
							+ Constants.ZABBIX_SERVER_PORT + " " + ex);
			SmartLogger.logThis(Level.WARN, "I will use the default port "
					+ Constants.ZABBIX_SERVER_DEFAULT_PORT);
			return Constants.ZABBIX_SERVER_DEFAULT_PORT;
		}
	}

	public Hashtable<String, Integer> getZabbixServers() throws Exception {
		String zxblist = new String();
		try {
			zxblist = new String(_props
					.getProperty(Constants.ZABBIX_SERVER_LIST));
		} catch (Exception e) {
			SmartLogger.logThis(Level.ERROR, "Error on getZabbixServers while getting "
					+ Constants.ZABBIX_SERVER_LIST + " " + e.getMessage());
		}
		StringTokenizer st = new StringTokenizer(zxblist, Constants.DELIMITER);
		Hashtable<String, Integer> ZabbixServers = new Hashtable<String, Integer>();
		int count = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken().toString();
			String server = new String();
			try {
				server = new String(_props.getProperty(token + "."
						+ Constants.ZABBIX_SERVER_HOST));
			} catch (Exception e) {
				SmartLogger.logThis(Level.ERROR, "Error on getZabbixServers while getting "
						+ token + "." + Constants.ZABBIX_SERVER_HOST + " "
						+ e.getMessage());
			}
			Integer port = new Integer(Constants.ZABBIX_SERVER_DEFAULT_PORT);
			try {
				port = new Integer(_props.getProperty(token + "."
						+ Constants.ZABBIX_SERVER_PORT));
			} catch (Exception e) {
				SmartLogger.logThis(Level.WARN,
						"Warning on getZabbixServers while getting " + token
								+ "." + Constants.ZABBIX_SERVER_PORT + " "
								+ e.getMessage());
				SmartLogger.logThis(Level.WARN, "Warning I will use the default port"
						+ port);
			}
			ZabbixServers.put(server, port);
			count++;
		}
		// fisdb.close();
		return ZabbixServers;
	}

	public boolean hasQueryFile(String dbName) {
		if (dbName == null) {
			return false;
		}
		try {
			verifyConfig();
			if (_props.getProperty(dbName + "." + Constants.QUERY_LIST_FILE) != null) {
				return true;
			}
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR, "Error on Configurator getting"+ Constants.QUERY_LIST_FILE
					+ dbName + ex.getMessage());
			return false;
		}
		return false;
	}

	public boolean isEqualsDBList(DBConn[] _dbc) throws Exception {
		try {
			verifyConfig();

			String[] localdblist = this.getDBList();
			String[] remotedblist = new String[_dbc.length];
			for (int i = 0; i < _dbc.length; i++) {
				remotedblist[i] = _dbc[i].getName();
			}
			return ArrayUtils.isEquals(localdblist, remotedblist);
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR,
					"Error on Configurator while comparing the databases lists on isEqualsDBList "
							+ ex);
			return false;
		}
	}

	public DBConn[] rebuildDBList(DBConn[] _dbc) {
		try {
			verifyConfig();
			String[] localdblist = this.getDBList();
			String[] remotedblist = new String[_dbc.length];
			for (int i = 0; i < _dbc.length; i++) {
				remotedblist[i] = _dbc[i].getName();
			}

			Collection<DBConn> connections = new ArrayList<DBConn>();
			for (int j = 0; j < localdblist.length; j++) {
				if (ArrayUtils.contains(remotedblist, localdblist[j])) {
					DBConn tmpDBConn;
					tmpDBConn = _dbc[ArrayUtils.indexOf(remotedblist,
							localdblist[j])];
					connections.add(tmpDBConn);
				}
				if (!ArrayUtils.contains(remotedblist, localdblist[j])) {
					/*
					 * adding database
					 */
					SmartLogger.logThis(Level.INFO, "New database founded! "
							+ localdblist[j]);
					DBConn tmpDBConn = this.getConnection(localdblist[j]);
					if (tmpDBConn != null) {
						connections.add(tmpDBConn);
					}
				}
			}
			for (int x = 0; x < _dbc.length; x++) {
				if (!ArrayUtils.contains(localdblist, _dbc[x].getName())) {
					SmartLogger.logThis(Level.WARN, "Database " + _dbc[x].getName()
							+ " removed from configuration file");
					/**
					 * removing database
					 */
					// _dbc[x].closeAll();
					_dbc[x].getSPDS().close();
					SmartLogger.logThis(Level.WARN, "Database " + _dbc[x].getName()
							+ " conections closed");
					_dbc[x] = null;
				}
			}
			DBConn[] connArray = (DBConn[]) connections.toArray(new DBConn[0]);
			return connArray;
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR,
					"Error on Configurator while retriving the databases list "
							+ Constants.DATABASES_LIST + " error:" + ex);
			return _dbc;
		}

	}
	public static Integer  getSleep() throws Exception {
		try{
			verifyConfig();
			Integer sleep = new Integer(5);
			try{
				sleep=new Integer(_props.getProperty(Constants.ORABBIX_DAEMON_SLEEP));
			}catch (Exception e){
				SmartLogger.logThis(Level.WARN,"Warning while getting "+Constants.ORABBIX_DAEMON_SLEEP+" I will use the default "+sleep);
			}
			return sleep;

		} catch (Exception ex){

			SmartLogger.logThis(Level.ERROR,"Error on Configurator while retriving "+Constants.ORABBIX_DAEMON_SLEEP+" "+ex);
			return null;
		}
	}

}
