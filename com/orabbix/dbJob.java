package com.orabbix;

import java.sql.Connection;

import org.apache.log4j.Logger;

public class dbJob  implements Runnable {
    private final Connection _dbConn;
    private Query[] _queries;
    private final Trapper _trapper;
    private final String _dbname;
    
	
    public dbJob(Connection dbConn, Query[] queries, Trapper trapper,String dbname) {
      this._dbConn = dbConn;
      this._queries = queries;
      this._trapper = trapper;
      this._dbname = dbname;

    }
    
    public void run() {
     Logger logger = Logger.getLogger("Orabbix");
     logger.debug("Starting dbJob on database "+_dbname);
     final long start = System.currentTimeMillis();
      try {
    	  
    	  
    	  Item[] zitems = DBEnquiry.execute(this._queries ,this._dbConn );
    	  logger.debug("Item retrieved "+zitems.length+" on database "+this._dbname);
    	  
    	  for ( int cnt=0; cnt < zitems.length; cnt++) {
    		  	//logger.debug("dbname "+this._dbname+ "sending item  "+zitems[cnt].getKey()+" value "+zitems[cnt].getValue());
				_trapper.send(zitems[cnt].getKey(),zitems[cnt].getValue());
			}
			_trapper.stop();
			_dbConn.close();
      } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}finally {
    	  if (_dbConn != null)
			try {
				_dbConn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	  if (_queries != null)
    		  _queries=null;
    	  if (_trapper !=null){
    		  _trapper.stop();
    	  }
    		  
     
	}
      logger.info("Done with dbJob on database "+_dbname+" elapsed time "+(System.currentTimeMillis() - start) + " ms");
    }
  }
