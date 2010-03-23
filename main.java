

/* This file is part of orabbix.
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
 
import java.sql.Connection;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Logger;

import com.orabbix.Configurator;
import com.orabbix.DBConn;
import com.orabbix.Query;
import com.orabbix.Trapper;
import com.orabbix.dbJob;



public class main {

	public static void printUsage()
    {
    System.out.println("USAGE");
    System.out.println("run.sh -Dlog4j.properties configfile");
    System.out.println("log4j.properties = log4j configuration file");
    System.out.println("configfile 		 = configuration parameter file");
	System.err.println("ARGS1 parameterfile");
   }

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(final String[] args) {
		 try {
			if (args.length == 0) {
			    printUsage();
			    System.exit(0);
			}
            Logger logger = Logger.getLogger("Orabbix");
            logger.info("Starting Orabbix.");
            
            
			String configFile;
			configFile= new String(args[0].toString());
			
			
			Configurator cfg =new Configurator(configFile);
			String [] DatabaseList = cfg.getDBList();
			
         	 ExecutorService executor = 
	               Executors.newFixedThreadPool(DatabaseList.length);
			DBConn[] myDBConn = cfg.getConnections();
			Hashtable<String, SharedPoolDataSource> htDBConn = new Hashtable<String, SharedPoolDataSource>();
			for (int i=0; i<myDBConn.length ;i++){
				htDBConn.put(myDBConn[i].getName(), myDBConn[i].getSPDS());
			}
			cfg=null;
			
			while (true){
			Configurator c =new Configurator(configFile);
			Query[] q =c.getOracleQueries();
			
			for (int i=0; i<DatabaseList.length ;i++){
				
				
				SharedPoolDataSource spds = 
					(SharedPoolDataSource) htDBConn.get(DatabaseList[i]);
				
				logger.debug("retrieve connection for dbname ->"+DatabaseList[i]);
				//System.out.println("retrieve connection for dbname ->"+DatabaseList[i]);
			try {
				Connection con =spds.getConnection();
				logger.debug("sharedpooldatasource idle connection -->"+spds.getNumIdle()+" active connetion -->"+spds.getNumActive()+""+" dbname -->"+DatabaseList[i]);
				//System.out.println("sharedpooldatasource idle connection -->"+spds.getNumIdle()+" active connetion -->"+spds.getNumActive()+""+" dbname -->"+DatabaseList[i]);
				//Item[] zitems = DBEnquiry.execute(queries ,myDBConn );
				//logger.debug("Item retrieved "+zitems.length);
				
				
				logger.debug("Starting ZabbixTrapper for "+DatabaseList[i]);
				final Trapper trapper = c.getTrapper(DatabaseList[i]);
				//final Trapper trapper = cfg.getTrapper("VM6465");
				Runnable runner = new dbJob(con,q, trapper,DatabaseList[i] );

				executor.execute(runner);
				
				//executor.wait();
				/*for ( int cnt1=0; cnt1 < zitems.length; cnt1++) {
					trapper.send(zitems[cnt1].getKey(),zitems[cnt1].getValue());
				}
				trapper.stop();*/
			}catch (Exception e){
				
				 logger.error("Error while retrieve the connection ->" + e.getMessage());
			}
			}
			
			logger.debug("going in bed...and sleep for "+c.getSleep()*1000+ " ms");
			//System.out.println("going in bed...and sleep for "+c.getSleep()*1000/60000+ " m");
			Thread.sleep(c.getSleep()*1000);
			logger.debug("waking up Goood Morning");
			
			}
			 
		 }catch (Exception ex){
			
			 ex.printStackTrace();
		 }
            
	}
	

}


