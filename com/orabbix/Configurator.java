/*
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

package com.orabbix;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Logger;


public class Configurator {
	private static  Properties _props;
	private static  Properties _propsq;

	public Configurator(String _url) throws IOException {
		Properties props = new Properties();
		Properties propsq = new Properties();
		FileInputStream fis,fisq;
		try {
			fis = new FileInputStream (new java.io.File( _url.toString()));
			props.load(fis);
			fisq = new FileInputStream (new java.io.File( props.getProperty(Constants.ORACLE+Constants.QUERY_LIST_FILE)));
			propsq.load(fisq);
			_props=props;
			_propsq=propsq;
			fis.close();
			fisq.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Logger logger = Logger.getLogger("Orabbix");
   	        logger.error("Error on Configurator  "+e.getMessage());
			e.printStackTrace();
		}
    }
	
	private static  void verifyConfig(){
		if (_props == null || _propsq == null){
			 throw new IllegalArgumentException("empty properties");
		}
			
	}
	public  Query[] getOracleQueries() throws Exception {
    	try{
    	verifyConfig();
		StringTokenizer stq = new StringTokenizer(_propsq.getProperty(Constants.QUERY_LIST),Constants.DELIMITER);
		String [] QueryLists = new   String[stq.countTokens()];
        int count = 0;
          while (stq.hasMoreTokens())
          {
          	String token = stq.nextToken().toString().trim();
          	QueryLists[count] = token;
          	count ++;
          }
          Collection<Query> Queries = new ArrayList<Query>();  
          for(int i=0; i<QueryLists.length; i++) {
        	Query q = new Query(  
              	      (String) _propsq.getProperty(QueryLists[i]+"."+Constants.QUERY_POSTFIX),
              	      (String) QueryLists[i]
              	      );
          	Queries.add( q );
          }	
 		Query[] queries  = (Query[]) Queries.toArray( new Query[0] );
        return queries;
    	} catch (Exception ex){
    		 Logger logger = Logger.getLogger("Orabbix");
    	     logger.error("Error on Configurator on getOracleQueries "+ex.getMessage());
    		return null;
    	}
        
    }

	
	
	public static  DBConn getConnection(String dbName) throws Exception {
		try{
			verifyConfig();
		 Logger logger = Logger.getLogger("Orabbix");
	     logger.debug("getConnection for database "+dbName);
	     String url= new String(_props.getProperty(dbName+"."+Constants.QUERY_URL));
    	 String uname= new String(_props.getProperty(dbName+"."+Constants.QUERY_USERNAME));
    	 String password= new String(_props.getProperty(dbName+"."+Constants.QUERY_PASSWORD));
    		DriverAdapterCPDS cpds = new DriverAdapterCPDS();
    		cpds.setDriver("oracle.jdbc.OracleDriver");
    		cpds.setUrl(url.toString());
    		cpds.setUser(uname.toString());
    		cpds.setPassword(password.toString());
    		SharedPoolDataSource tds = new SharedPoolDataSource();
    		tds.setConnectionPoolDataSource(cpds);
    		//tds.setMaxActive(5);
    		Integer maxActive= new Integer(_props.getProperty(Constants.DATABASES_LIST+".MaxActive"));
    		tds.setMaxActive(maxActive.intValue());
    		Integer maxWait= new Integer(_props.getProperty(Constants.DATABASES_LIST+".MaxWait"));
    		tds.setMaxWait(maxWait.intValue());
    		Integer maxIdle= new Integer(_props.getProperty(Constants.DATABASES_LIST+".MaxIdle"));
    		tds.setMaxIdle(maxIdle.intValue());
    		tds.setValidationQuery("select sysdate from dual");
            Connection con = null;
            con = tds.getConnection();
            PreparedStatement p_stmt = null; 
            p_stmt = con.prepareStatement("SELECT SYS_CONTEXT ('USERENV', 'SESSION_USER') FROM DUAL");
            ResultSet rs = null;
            rs = p_stmt.executeQuery();
            while(rs.next()){
            	logger.info("Connected as "+rs.getString("SYS_CONTEXT('USERENV','SESSION_USER')"));
            	//System.out.println("Connected as "+rs.getString("SYS_CONTEXT('USERENV','SESSION_USER')"));
            }
            p_stmt = con.prepareStatement("SELECT SYS_CONTEXT ('USERENV', 'DB_NAME') FROM DUAL");
            rs = p_stmt.executeQuery();
            while(rs.next()){
            	logger.info("--------- on Database -> "+rs.getString("SYS_CONTEXT('USERENV','DB_NAME')"));
            	//System.out.println("--------- on Database -> "+rs.getString("SYS_CONTEXT('USERENV','DB_NAME')"));
            }
            DBConn mydbconn = new DBConn(tds , dbName.toString());
    	    return mydbconn;
    	    
	} catch (Exception ex){
		 Logger logger = Logger.getLogger("Orabbix");
	     logger.error("Error on Configurator for database "+dbName+" -->"+ex.getMessage());
	     logger.error("This Database "+dbName+" will be removed");
         return null;
	}
	}
        
  public  DBConn[] getConnections() throws Exception {
		try{
			verifyConfig();
	  Logger logger = Logger.getLogger("Orabbix");
      logger.debug("Starting configurator...");
      String [] DatabaseList = getDBList();
        	Collection<DBConn> connections = new ArrayList();
        for(int i=0; i<DatabaseList.length; i++) {
        	connections.add(Configurator.getConnection(DatabaseList[i]));
        }
        //fis.close();
    	DBConn[] connArray  = (DBConn[]) connections.toArray( new DBConn[0] );
        return connArray;
		} catch (Exception ex){
			Logger logger = Logger.getLogger("Orabbix");
		    logger.error("Error on Configurator getConnections "+ex.getMessage());
			return null;
		}
    }
  
	public String [] getDBList() throws Exception {
		try{
			verifyConfig();
        StringTokenizer st = new StringTokenizer(_props.getProperty(Constants.DATABASES_LIST),Constants.DELIMITER);
	    String [] DatabaseList = new   String[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens())
	        {
	        	String token = st.nextToken().toString();
	        	DatabaseList[count] = token;
	        	count ++;
	        }
        //fisdb.close();
        return DatabaseList;
	} catch (Exception ex){
		Logger logger = Logger.getLogger("Orabbix");
	    logger.error("Error on Configurator while retriving the databases list "+Constants.DATABASES_LIST+" "+ex.getMessage());
		return null;
	}
	}
	
	
	public static Trapper  getTrapper(String _host) throws Exception {
		try{
			verifyConfig();
			return new ZabbixTrapper(_props.getProperty(Constants.ZABBIX_SERVER_HOST), _host);
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	public static Integer  getSleep() throws Exception {
		try{
			verifyConfig();
			return new Integer(_props.getProperty(Constants.ZABBIX_DAEMON_SLEEP));
		} catch (Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
  

}
