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
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Level;

public class DBJob  implements Runnable {
    private final Connection _dbConn;
    private Query[] _queries;
    private final BlockingQueue<Item> _queue = new LinkedBlockingQueue<Item>();
    private final String _dbname;
    private final String _queriesGroup;
    private final int _dgNum;
    private final Hashtable <String , Integer >  _zabbixServers;

    public DBJob(Connection dbConn, Query[] queries,String queriesGroup, Hashtable <String , Integer >  zabbixServers, String dbname) {
        this._dbConn = dbConn;
        this._queries = queries;
        this._queriesGroup = queriesGroup;
        this._zabbixServers = zabbixServers;

        this._dbname = dbname;
        this._dgNum = 0;
      }
      
    
    public DBJob(Connection dbConn, Query[] queries,String queriesGroup,Hashtable <String , Integer >  zabbixServers, String dbname, int dgNum) {
      this._dbConn = dbConn;
      this._queries = queries;
      this._zabbixServers = zabbixServers;
      this._queriesGroup = queriesGroup;
      this._dbname = dbname;
      if (dgNum>0){
    	  this._dgNum = dgNum;
      }
      else {
    	  this._dgNum = 0;
      }
    }
    
    public void run() {
     Configurator.logThis(Level.DEBUG,"Starting dbJob on database "+_dbname+" "+_queriesGroup);
     final long start = System.currentTimeMillis();
      try {
   		  Item[] zitems = DBEnquiry.execute(this._queries ,this._dbConn );
    	  Configurator.logThis(Level.DEBUG,"Item retrieved "+zitems.length+" on database "+this._dbname);
    	  for ( int cnt=0; cnt < zitems.length; cnt++) {
//    		  BlockingQueue itemQueue = null;
//				itemQueue.add(zitems[cnt]);
    		  String zItemName=zitems[cnt].getKey();
    		  if (this._dgNum>0) {
    			  zItemName = zItemName+"_"+_dgNum;
    		  }
    		  Configurator.logThis(Level.DEBUG,"dbname "+this._dbname+ "sending item  "+zitems[cnt].getKey()+" value "+zitems[cnt].getValue());
    	  		_queue.offer(new Item(zitems[cnt].getKey(), zitems[cnt].getValue()));
			}
    	  /*Postman postman = new Postman(_zabbixServer, _zabbixPort, this._dbname);
    	  postman.send(_queue);
    	  */
    	  Sender sender = new Sender(_queue,_zabbixServers, this._dbname);
    	  sender.run();
    	  
		_dbConn.close();
      } catch (Exception e) {
		// TODO Auto-generated catch block
    	  Configurator.logThis(Level.ERROR,"Error on dbJob for database "+_dbname+" "+_queriesGroup+" error: "+e);
	  	}finally {
    	  if (_dbConn != null)
			try {
				_dbConn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
		    	  Configurator.logThis(Level.ERROR,"Error on dbJob for database "+_dbname+" "+_queriesGroup+" error: "+e);
			}
    	  if (_queries != null)
    		  _queries=null;
    	  /*if (_trapper !=null){
    		  _trapper.stop();
    	  }*/
     
	}
  	  Configurator.logThis(Level.INFO,"Done with dbJob on database "+_dbname+" "+_queriesGroup+" elapsed time "+(System.currentTimeMillis() - start) + " ms");
    }
  }
