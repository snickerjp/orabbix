
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Level;

public class DBEnquiry {
	public static 	Item[] execute(Query[] _queries, Connection _conn)  {
	    if (_queries == null || _queries.length<1) {
            throw new IllegalArgumentException("Query's array is empty or null");
        }
        Connection con=_conn;
        Collection<Item> SZItems = new ArrayList<Item>();
         ResultSet rs = null;
        PreparedStatement p_stmt = null;
        
    	//System.out.println( " db : " + _dbConn.getName() );
         
         
    	 for (int i=0 ; i< _queries.length ;i++)
         {
       //	System.out.println(queries[i].getSQL());
    		 Configurator.logThis(Level.DEBUG,"Actual query is "+_queries[i].getName()+" statement is "+_queries[i].getSQL().toString());

    		 
       //	System.out.println(queries[i].getName());
    		 String tempStr=new String("");
    	try {
           p_stmt = con.prepareStatement(_queries[i].getSQL().toString());
           rs = p_stmt.executeQuery();
	            ResultSetMetaData rsmd = rs.getMetaData();
	            	int numColumns = rsmd.getColumnCount(); 
	            	while(rs.next()){
	            //	System.out.println(_queries[i].getSQL());
	            //	System.out.println(_queries[i].getName());
	            //	tempStr=rs.getObject(1).toString().trim();
           			for (int r=1; r<numColumns+1; r++) {
	        			tempStr=tempStr+rs.getObject(r).toString().trim();
           	      }
           			Configurator.logThis(Level.DEBUG,"resultset returned from query "+_queries[i].getName()+" resultset -->"+tempStr.toString());
	            
	            }
	            if (tempStr==null || tempStr.length()==0){
	            	if (_queries[i].getNoData().length()>0 && _queries[i].getNoData()!=null){
	            		tempStr=_queries[i].getNoData();
	            	}
	            }
	            
    	} catch (Exception ex){
			Configurator.logThis(Level.ERROR, "Error on DBEnquiry on query ="+_queries[i].getName()+" Error returned is "+ex);
    		if (_queries[i].getNoData().length()>0 && _queries[i].getNoData()!=null){
    			tempStr=_queries[i].getNoData();
    		}else {
    			tempStr="";
    		}
    		Configurator.logThis(Level.WARN, "I'm going to return "+tempStr+" for query "+_queries[i].getName());
			}
	            Item zitem = new Item(_queries[i].getName(),tempStr);
	            SZItems.add(zitem);
         }
    	 try{
    		 rs.close() ;
    		 con.close();
    	 }catch (Exception ex){
    		 Configurator.logThis(Level.ERROR, "Error on DBEnquiry while closing connection"+ex); 
    	 }
    	          Item[] items= (Item[]) SZItems.toArray( new Item[0] );
    	          return items;        
	}
}
