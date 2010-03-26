
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

public class DBEnquiry {
	public static 	Item[] execute(Query[] _queries, DBConn[] _dbConn) throws Exception {
	    if (_queries == null || _queries.length<1) {
            throw new IllegalArgumentException("Query's array is empty or null");
        }
        if (_dbConn == null||_dbConn.length<1) {
            throw new IllegalArgumentException("DBConnection is empty or null");
        }
        
        int count;
        Collection<Item> SZItems = new ArrayList();
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement p_stmt = null;
		for ( count=0; count < _dbConn.length; count++) {
    	         System.out.println( (1+count) + " db : " + _dbConn[count].getName() );
    	          con = _dbConn[count].getSPDS().getConnection();
    	          for (int i=0 ; i< _queries.length ;i++)
    	          {
    	        //	System.out.println(queries[i].getSQL());
    	        //	System.out.println(queries[i].getName());
    	            p_stmt = con.prepareStatement(_queries[i].getSQL().toString());
    	            rs = p_stmt.executeQuery();
    	            String tempStr=new String("");
        	            while(rs.next()){
        	            //	System.out.println(_queries[i].getSQL());
        	            //	System.out.println(_queries[i].getName());
        	            	tempStr=rs.getObject(1).toString().trim();
        	            	while ( rs.next() ) {
        	            		tempStr=tempStr+rs.getObject(1).toString().trim();
        	            	      }
        	            //	System.out.println( tempStr.toString());
        	            }
        //	            System.out.println("_dbConn[count].getName()"+_dbConn[count].getName()+"_queries[i].getName()"+_queries[i].getName()+"tempStr"+tempStr);
        	            Item zitem = new Item(_queries[i].getName(),tempStr, _dbConn[count].getName());
        	            SZItems.add(zitem);
        	            rs.close() ;
        	         
    	          }
    	          con.close();
    	      }
		Item[] items= (Item[]) SZItems.toArray( new Item[0] );
        return items;        
	}
	
	
	/*
	 * NOT USED
	 * */
	public static 	Item[] execute(Query[] _queries, DBConn _dbConn) throws Exception {
	    if (_queries == null || _queries.length<1) {
            throw new IllegalArgumentException("Query's array is empty or null");
        }
        

        Collection SZItems = new ArrayList();
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement p_stmt = null;
        
    	//System.out.println( " db : " + _dbConn.getName() );
         con = _dbConn.getSPDS().getConnection();
         
    	 for (int i=0 ; i< _queries.length ;i++)
         {
       //	System.out.println(queries[i].getSQL());
    		 
       //	System.out.println(queries[i].getName());
           p_stmt = con.prepareStatement(_queries[i].getSQL().toString());
           rs = p_stmt.executeQuery();
           String tempStr=new String("");
	            while(rs.next()){
	            //	System.out.println(_queries[i].getSQL());
	            //	System.out.println(_queries[i].getName());
	            	tempStr=rs.getObject(1).toString().trim();
	            	while ( rs.next() ) {
	            		tempStr=tempStr+rs.getObject(1).toString().trim();
	            	      }
	            //	System.out.println( tempStr.toString());
	            }
	            Item zitem = new Item(_queries[i].getName(),tempStr, _dbConn.getName());
	            SZItems.add(zitem);
	            rs.close() ;
	         
         }
    	          con.close();
    	          Item[] items= (Item[]) SZItems.toArray( new Item[0] );
    	          return items;        
	}
	
	public static 	Item[] execute(Query[] _queries, Connection _conn) throws Exception {
	    if (_queries == null || _queries.length<1) {
            throw new IllegalArgumentException("Query's array is empty or null");
        }
        Connection con=_conn;
        Collection<Item> SZItems = new ArrayList();
         ResultSet rs = null;
        PreparedStatement p_stmt = null;
        
    	//System.out.println( " db : " + _dbConn.getName() );
         
         
    	 for (int i=0 ; i< _queries.length ;i++)
         {
       //	System.out.println(queries[i].getSQL());
        	 Logger logger = Logger.getLogger("Orabbix");
    	     logger.debug("Actual query is "+_queries[i].getName()+" statement is "+_queries[i].getSQL().toString());

    		 
       //	System.out.println(queries[i].getName());
           p_stmt = con.prepareStatement(_queries[i].getSQL().toString());
           rs = p_stmt.executeQuery();
           String tempStr=new String("");
	            while(rs.next()){
	            //	System.out.println(_queries[i].getSQL());
	            //	System.out.println(_queries[i].getName());
	            	tempStr=rs.getObject(1).toString().trim();
	            	while ( rs.next() ) {
	            		tempStr=tempStr+rs.getObject(1).toString().trim();
	            	      }
	            //	System.out.println( tempStr.toString());
	            }
	            Item zitem = new Item(_queries[i].getName(),tempStr);
	            SZItems.add(zitem);
                rs.close() ;
	         
         }
    	 con.close();
    	          Item[] items= (Item[]) SZItems.toArray( new Item[0] );
    	          return items;        
	}
	
	

}
