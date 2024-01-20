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

package com.smartmarmot.common.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;

import com.smartmarmot.common.SmartLogger;
import com.smartmarmot.orabbix.Constants;
import com.smartmarmot.orabbix.Query;
import com.smartmarmot.zabbix.ZabbixItem;



public class DBEnquiry {


	
	public static String ask(String _query, Connection _con, String queryName,
			String dbName, Boolean trim, Boolean space,
			List<Integer> _excludeColumns) {
		String tempStr="";
		try{
			ResultSet rs = null;
			PreparedStatement p_stmt = _con.prepareStatement(_query);
			rs = p_stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int numColumns = rsmd.getColumnCount();
			while (rs.next()) {
				for (int r = 1; r < numColumns + 1; r++) {
					Integer tmpInteger = new Integer(r);
					if (!_excludeColumns.contains(tmpInteger)) {
						if (trim) {
							tempStr = tempStr
									+ rs.getObject(r).toString().trim();
						} else {
							tempStr = tempStr + rs.getObject(r).toString();
						}
						if (space && (r < numColumns)) {
							tempStr = tempStr + ' ';
						}
					}
				}
			}
			try{
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
				SmartLogger.logThis(Level.ERROR,
						"Error on DBEnquiry while closing resultset "
						+ ex.getMessage() + " on database=" + dbName);
			}
		} catch (Exception ex) {
			SmartLogger.logThis(Level.WARN,
					"Error while executing ->"
					+ queryName 
					+ "- on database ->"
					+ dbName
					+ "- Exception received "
					+ ex.getMessage());
			tempStr = null;
		}
		return tempStr;
	}




	public static ZabbixItem[] execute(Query[] _queries, Connection _conn,
			String dbname) {
		if (_queries == null || _queries.length < 1) {
			throw new IllegalArgumentException("Query's array is empty or null");
		}
		Connection con = _conn;

		Collection<ZabbixItem> SZItems = new ArrayList<ZabbixItem>();
		for (int i = 0; i < _queries.length; i++) {
			// System.out.println(queries[i].getSQL());
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String datetime = dateFormat.format(_queries[i].getNextrun());

			// Configurator.logThis(Level.DEBUG,"Actual query is "+_queries[i].getName()+" on database="+
			// dbname+" Period="+_queries[i].getPeriod()+" nextRun="+datetime);
			// System.out.println(queries[i].getName());
			String tempStr = new String("");
			// check if is the right time to execute the statements
			try {

				if (_queries[i].getActive()) {
					// if item is active
					Date now = new Date(System.currentTimeMillis());
					Date nextRun = _queries[i].getNextrun();
					if (now.after(nextRun)) {

						Date newNextRun = new Date(nextRun.getTime()
								+ (_queries[i].getPeriod() * 1000 * 60));
						_queries[i].setNextrun(newNextRun);

						datetime = dateFormat.format(_queries[i].getNextrun());
						SmartLogger.logThis(Level.DEBUG, "Actual query is "
								+ _queries[i].getName() + "Nextrun " + datetime
								+ " on database=" + dbname + " Period="
								+ _queries[i].getPeriod());

						/*
						 * execute RaceConditionQuery
						 */
						boolean racecond = true;
						String result = "";
						if (_queries[i].getRaceQuery() != null) {
							if (_queries[i].getRaceQuery().length() > 0) {
								SmartLogger.logThis(Level.DEBUG, "INFO:"
										+ _queries[i].getName()
										+ " RaceCondiftionQuery ->"
										+ _queries[i].getRaceQuery());
								result = ask(
										_queries[i].getRaceQuery(),
										_conn,
										_queries[i].getName()
												+ Constants.RACE_CONDITION_QUERY,
										dbname, _queries[i].getTrim(),
										_queries[i].getSpace(), 
										_queries[i]
												.getRaceExcludeColumnsList());
								if (result != null) {
									if (_queries[i].getRaceValue() != null) {
										if (!result.equalsIgnoreCase(_queries[i]
												.getRaceValue())) {
											racecond = false;
										}
									}
								}
							}
						}
						result = "";
						if (racecond) {
							result = ask(_queries[i].getSQL().toString(),
									_conn, _queries[i].getName(), dbname,
									_queries[i].getTrim(), _queries[i]
											.getSpace(), 
											_queries[i]
											.getExcludeColumnsList());
							if (result == null) {
								if (_queries[i].getNoData().length() > 0
										&& _queries[i].getNoData() != null) {
									result = _queries[i].getNoData();
								}
							} else if (result.length() == 0) {
								if (_queries[i].getNoData().length() > 0
										&& _queries[i].getNoData() != null) {
									result = _queries[i].getNoData();
								}
							}

							ZabbixItem zitem = new ZabbixItem(_queries[i]
									.getName(), result,dbname);
							SZItems.add(zitem);
							SmartLogger.logThis(Level.DEBUG,
									"I'm going to return " + result
											+ " for query "
											+ _queries[i].getName()
											+ " on database=" + dbname);
						}
					}

				}
			} catch (Exception ex) {
				SmartLogger.logThis(Level.ERROR,
						"Error on DBEnquiry on query=" + _queries[i].getName()
						+ " on database=" + dbname
						+ " Error returned is " + ex);
				if (_queries[i].getNoData() != null)
				{
					if (_queries[i].getNoData().length() > 0)
						tempStr = _queries[i].getNoData();
				} else {
					tempStr = "";
				}
				ZabbixItem zitem = new ZabbixItem(_queries[i].getName(),
						tempStr , dbname);
				SZItems.add(zitem);
				SmartLogger.logThis(Level.DEBUG, "I'm going to return "
						+ tempStr + " for query " + _queries[i].getName()
						+ " on database=" + dbname);
			}

		}
		try {
			if (!con.isClosed())
				con.close();
		} catch (Exception ex) {
			SmartLogger.logThis(Level.ERROR,
					"Error on DBEnquiry while closing connection "
					+ ex.getMessage() + " on database=" + dbname);
		}
		ZabbixItem[] items = SZItems.toArray(new ZabbixItem[0]);
		return items;
	}
}
