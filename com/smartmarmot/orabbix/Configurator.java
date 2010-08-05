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

package com.smartmarmot.orabbix;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.dbcp.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.smartmarmot.orabbix.Constants;


public class Configurator {
	private static  Properties _props;
	private static  Properties _propsq;

	public Configurator(String _url) throws IOException {
		Properties props = new Properties();
		Properties propsq = new Properties();
		FileInputStream fis,fisq;
		try {
			try{
				fis = new FileInputStream (new java.io.File( _url.toString()));
				props.load(fis);
				fis.close();
			}catch (Exception e){
				logThis(Level.ERROR,"Error on Configurator while retirving configuration file "+_url+" "+e.getMessage());	
			}
			try{
				String prpq = new String(props.getProperty(Constants.QUERY_LIST_FILE));
				fisq = new FileInputStream (new java.io.File( prpq));
				propsq.load(fisq);
				fisq.close();
			}catch (Exception e){
				logThis(Level.ERROR,"Error on Configurator while retirving query file "+Constants.QUERY_LIST_FILE+" "+e.getMessage());	
			}
			_props=props;
			_propsq=propsq;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logThis(Level.ERROR,"Error on Configurator "+e.getMessage());
		}
    }
	
	private static  void verifyConfig(){
		if (_props == null || _propsq == null){
			 throw new IllegalArgumentException("empty properties");
		}
			
	}
	public static void logThis(Level level,String message){
		Logger logger = Logger.getLogger(Constants.PROJECT_NAME);	
		if (message==null){
			message ="";
		}
	    logger.log(level, message);
	}
	
	public  Query[] getQueries() throws Exception {
    	try{
    	verifyConfig();
    	String ql="";
    	try{ 
    		ql = new String(_propsq.getProperty(Constants.QUERY_LIST));
    	} catch (Exception ex){
    		logThis(Level.ERROR,"Error on getQueries while getting "+Constants.QUERY_LIST +" "+ex.getMessage());
    	}
		StringTokenizer stq = new StringTokenizer(ql,Constants.DELIMITER);
		String [] QueryLists = new String[stq.countTokens()];
        int count = 0;
          while (stq.hasMoreTokens())
          {
          	String token = stq.nextToken().toString().trim();
          	QueryLists[count] = token;
          	count ++;
          }
          Collection<Query> Queries = new ArrayList<Query>();  
          for(int i=0; i<QueryLists.length; i++) {
        	  String query="";
        	  try {
        		  query=new String( _propsq.getProperty(QueryLists[i]+"."+Constants.QUERY_POSTFIX));
        	  } catch (Exception ex){
        		  logThis(Level.ERROR,
        				  "Error while getting "+ QueryLists[i]+"."+Constants.QUERY_POSTFIX+" "+ex.getMessage());
        	  }
        	  String queryName=(String) QueryLists[i];
        	  String noDataFound="";
        	  try {
        		  noDataFound=new String(_propsq.getProperty(QueryLists[i]+"."+Constants.QUERY_NO_DATA_FOUND));
        	  } catch (Exception ex){
        		  //logThis(Level.WARN,"Warning while getting "+QueryLists[i]+"."+Constants.QUERY_NO_DATA_FOUND+" null or not present "+ex.getMessage());
        	  }
        	Query q = new Query(  
              	      query,
              	      queryName,
              	      noDataFound
              	      );
          	Queries.add( q );
          }	
 		Query[] queries  = (Query[]) Queries.toArray( new Query[0] );
        return queries;
    	} catch (Exception ex){
    		logThis(Level.ERROR,"Error on Configurator on getQueries "+ex.getMessage());
    		return null;
    	}
        
    }
	
	
	
	public  DBConn getConnection(String dbName) throws Exception {
		try{
			verifyConfig();
		 
	     logThis(Level.DEBUG,"getConnection for database "+dbName);
	     String url="";
	     try{
	    	url=new String(_props.getProperty(dbName+"."+Constants.CONN_URL)); 
	     } catch (Exception ex){
	    		logThis(Level.ERROR,"Error on Configurator getConnection while getting "+dbName+"."+Constants.CONN_URL+ " "+ex.getMessage());
	     }
	      
    	 String uname="";
    	 try{
    		 uname=new String(_props.getProperty(dbName+"."+Constants.CONN_USERNAME));
    	 } catch (Exception ex){
	    		logThis(Level.ERROR,"Error on Configurator getConnection while getting "+dbName+"."+Constants.CONN_USERNAME+ " "+ex.getMessage());
	     }
    	 String password= "";
    	 try{
    		 password=new String(_props.getProperty(dbName+"."+Constants.CONN_PASSWORD));
    	 } catch (Exception ex){
	    		logThis(Level.ERROR,"Error on Configurator getConnection while getting "+dbName+"."+Constants.CONN_PASSWORD+ " "+ex.getMessage());
	     }	 
    		DriverAdapterCPDS cpds = new DriverAdapterCPDS();
    		cpds.setDriver(Constants.ORACLE_DRIVER);
    		cpds.setUrl(url.toString());
    		cpds.setUser(uname.toString());
    		cpds.setPassword(password.toString());
    		SharedPoolDataSource tds = new SharedPoolDataSource();
    		tds.setConnectionPoolDataSource(cpds);
    		//tds.setMaxActive(5);
    		Integer maxActive=new Integer(5);
    		try {
    			maxActive= new Integer(_props.getProperty(dbName+"."+Constants.CONN_MAX_ACTIVE));
    		} catch(Exception ex){
    			logThis(Level.INFO,"Note: "+dbName+"."+Constants.CONN_MAX_ACTIVE+ " "+ex.getMessage());
    			try {
    				 maxActive= new Integer(_props.getProperty(Constants.DATABASES_LIST+"."+Constants.CONN_MAX_ACTIVE));
    			} catch(Exception e){
    				logThis(Level.WARN,"Warning while getting "+Constants.DATABASES_LIST+"."+Constants.CONN_MAX_ACTIVE+" "+e.getMessage());
    				 logThis(Level.WARN,"Warning I will use default value "+maxActive);
    			}
    		}	
    		tds.setMaxActive(maxActive.intValue());
    		Integer maxWait= new Integer(100);
    		try {
    			maxWait= new Integer(_props.getProperty(dbName+"."+Constants.CONN_MAX_WAIT));
    		} catch(Exception ex){
    			logThis(Level.INFO,"Note: "+dbName+"."+Constants.CONN_MAX_WAIT+ " "+ex.getMessage());
    			try {
    				maxWait= new Integer(_props.getProperty(Constants.DATABASES_LIST+"."+Constants.CONN_MAX_WAIT));
    			} catch(Exception e){
    				logThis(Level.WARN,"Warning while getting "+Constants.DATABASES_LIST+"."+Constants.CONN_MAX_WAIT+" "+e.getMessage());
    				 logThis(Level.WARN,"Warning I will use default value "+maxWait);
    			}
    		}	
    		tds.setMaxWait(maxWait.intValue());
    		Integer maxIdle= new Integer(1);
    		try {
    			maxIdle= new Integer(_props.getProperty(dbName+"."+Constants.CONN_MAX_IDLE));
    		} catch(Exception ex){
    			logThis(Level.INFO,"Note: "+dbName+"."+Constants.CONN_MAX_IDLE+ " "+ex.getMessage());
    			try {
    				maxIdle= new Integer(_props.getProperty(Constants.DATABASES_LIST+"."+Constants.CONN_MAX_IDLE));
    			} catch(Exception e){
    				logThis(Level.WARN,"Warning while getting "+Constants.DATABASES_LIST+"."+Constants.CONN_MAX_IDLE+" "+e.getMessage());
    				 logThis(Level.WARN,"Warning I will use default value "+maxIdle);
    			}
    		}	
    		tds.setMaxIdle(maxIdle.intValue());
    		tds.setValidationQuery(Constants.ORACLE_VALIDATION_QUERY);
            Connection con = null;
            con = tds.getConnection();
            PreparedStatement p_stmt = null; 
            p_stmt = con.prepareStatement(Constants.ORACLE_WHOAMI_QUERY);
            ResultSet rs = null;
            rs = p_stmt.executeQuery();
            String tempStr=new String("");
            ResultSetMetaData rsmd = rs.getMetaData();
        	int numColumns = rsmd.getColumnCount(); 
        	while(rs.next()){
        		for (int r=1; r<numColumns+1; r++) {
        				tempStr=tempStr+rs.getObject(r).toString().trim();
        		}
        	}
            logThis(Level.INFO,"Connected as "+tempStr);
            	
            con.close();
            con = null;
            con = tds.getConnection();
            p_stmt = con.prepareStatement(Constants.ORACLE_DBNAME_QUERY);
            rs = p_stmt.executeQuery();
            rsmd = rs.getMetaData();
        	numColumns = rsmd.getColumnCount(); 
        	tempStr="";
        	while(rs.next()){
        		for (int r=1; r<numColumns+1; r++) {
        				tempStr=tempStr+rs.getObject(r).toString().trim();
        		}
        	}
            logThis(Level.INFO,"--------- on Database -> "+tempStr);
            con.close();
            con = null;
            DBConn mydbconn = new DBConn(tds , dbName.toString());
    	    return mydbconn;
    	    
	} catch (Exception ex){
		logThis(Level.ERROR,"Error on Configurator for database "+dbName+" -->"+ex.getMessage());
         return null;
	}
	}
        
  public  DBConn[] getConnections() throws Exception {
		try{
			verifyConfig();
      logThis(Level.DEBUG,"Starting configurator...");
      String [] DatabaseList = getDBList();
        	Collection<DBConn> connections = new ArrayList<DBConn>();
        for(int i=0; i<DatabaseList.length; i++) {
        	connections.add(this.getConnection(DatabaseList[i]));
        }
        //fis.close();
    	DBConn[] connArray  = (DBConn[]) connections.toArray( new DBConn[0] );
        return connArray;
		} catch (Exception ex){
			logThis(Level.ERROR, "Error on Configurator getConnections "+ex.getMessage());
			return null;
		}
    }
  
	public String [] getDBList() throws Exception {
		try{
			verifyConfig();
		String dblist="";
		try{
			dblist=new String(_props.getProperty(Constants.DATABASES_LIST));
		}catch (Exception e){
			logThis(Level.ERROR,"Error on Configurator while retriving the databases list "+Constants.DATABASES_LIST+" "+e);
		}
		
        StringTokenizer st = new StringTokenizer(dblist,Constants.DELIMITER);
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
		logThis(Level.ERROR,"Error on Configurator while retriving the databases list "+Constants.DATABASES_LIST+" "+ex);
		return null;
	}
	}
	
	public Hashtable <String , Integer >  getZabbixServers() throws Exception {
		String zxblist =new String();
		try{
			zxblist = new String(_props.getProperty(Constants.ZABBIX_SERVER_LIST));
		}catch (Exception e){
			logThis(Level.ERROR,"Error on getZabbixServers while getting "+Constants.ZABBIX_SERVER_LIST +" "+e.getMessage());
		}
	 StringTokenizer st = new StringTokenizer(zxblist,Constants.DELIMITER);
	    Hashtable <String , Integer > ZabbixServers =new Hashtable<String, Integer>();
     int count = 0;
     while (st.hasMoreTokens())
	        {
	        	String token = st.nextToken().toString();
	        	String server= new String();
	        		try {
	        			server = new String(_props.getProperty(token+"."+Constants.ZABBIX_SERVER_HOST));
	        		}catch (Exception e){
	        			logThis(Level.ERROR,"Error on getZabbixServers while getting "+token+"."+Constants.ZABBIX_SERVER_HOST+" "+e.getMessage());
	        		}
	       	 	Integer port= new Integer(Constants.ZABBIX_SERVER_DEFAULT_PORT);
	       	 		try {
	       	 		port=new Integer(_props.getProperty(token+"."+Constants.ZABBIX_SERVER_PORT));
	       	 	}catch (Exception e){
        			logThis(Level.WARN,"Warning on getZabbixServers while getting "+token+"."+Constants.ZABBIX_SERVER_PORT+" "+e.getMessage());
        			logThis(Level.WARN,"Warning I will use the default port"+port);
        		}
	        	ZabbixServers.put(server, port);
	        	count ++;
	        }
     //fisdb.close();
     return ZabbixServers;
	}
     
     
	public String  getZabbixServer() throws Exception {
		try{
			verifyConfig();
			return _props.getProperty(Constants.ZABBIX_SERVER_HOST);
			
		} catch (Exception ex){
				logThis(Level.ERROR,"Error on Configurator while retriving zabbix server host "+Constants.ZABBIX_SERVER_HOST+" or port "+Constants.ZABBIX_SERVER_PORT +" "+ex);
			return null;
		}
	}
	
	
	public int  getZabbixServerPort() throws Exception {
		try{
			verifyConfig();
			Integer port= new Integer(_props.getProperty(Constants.ZABBIX_SERVER_PORT));
			return port.intValue();
		} catch (Exception ex){
			logThis(Level.ERROR,"Error on Configurator while retriving zabbix server port "+Constants.ZABBIX_SERVER_PORT +" "+ex);
			logThis(Level.WARN,"I will use the default port "+Constants.ZABBIX_SERVER_DEFAULT_PORT);
			return Constants.ZABBIX_SERVER_DEFAULT_PORT;
		}
	}
	
	public Integer getMaxThread() throws Exception {
		try{
			verifyConfig();
			return new Integer(_props.getProperty(Constants.ORABBIX_DAEMON_THREAD));
		} catch (Exception ex){
			logThis(Level.ERROR,"Error on Configurator while retriving the "+Constants.ORABBIX_DAEMON_THREAD+" "+ex);
			return null;
		}
	}
	
	public static Integer  getSleep() throws Exception {
		try{
			verifyConfig();
			Integer sleep = new Integer(5);
			try{
				sleep=new Integer(_props.getProperty(Constants.ORABBIX_DAEMON_SLEEP));
			}catch (Exception e){
				logThis(Level.WARN,"Warning while getting "+Constants.ORABBIX_DAEMON_SLEEP+"i will use the default "+sleep);
			}
			return sleep;
			
		} catch (Exception ex){
			logThis(Level.ERROR,"Error on Configurator while retriving "+Constants.ORABBIX_DAEMON_SLEEP+" "+ex);
			return null;
		}
	}
  

}
