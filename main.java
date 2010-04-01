

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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Logger;



import com.orabbix.Configurator;
import com.orabbix.DBConn;
import com.orabbix.Query;
import com.orabbix.Trapper;
import com.orabbix.ZabbixTrapper;
import com.orabbix.dbJob;



public class main {

	public static void printUsage()
    {
    System.out.println("USAGE");
    System.out.println("run.sh -Dlog4j.properties=configfile");
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
			Integer maxThread=cfg.getMaxThread();

			ExecutorService executor = 
	               Executors.newFixedThreadPool(maxThread.intValue());
			DBConn[] myDBConn = cfg.getConnections();
			
			Hashtable<String, SharedPoolDataSource> htDBConn = new Hashtable<String, SharedPoolDataSource>();
			
			for (int i=0; i<myDBConn.length ;i++){
				if (myDBConn[i]!=null){
				htDBConn.put(myDBConn[i].getName(), myDBConn[i].getSPDS());
				}
			}
			/**
			 * 
			 */
			cfg=null;
			
			/**
			 * daemon begin here
			 */
			while (true){
			/**
			 * istantiate a new configurator
			 */
				Configurator c =new Configurator(configFile);
				String [] dblist = c.getDBList();
				Query[] q =c.getOracleQueries();
			
			for (int i=0; i<dblist.length ;i++){
				if (!htDBConn.containsKey(dblist[i].toString())) {
					logger.warn("New Database Founded: adding database "+dblist[i].toString());
					DBConn newDBConn = c.getConnection(dblist[i].toString());
					if (newDBConn!=null){
						htDBConn.put(newDBConn.getName(), newDBConn.getSPDS());
					}
					}
			}
			/**
			 * to clean list from removed databases
			 */
			if (dblist.length< htDBConn.size()){
				Hashtable htTemp= new Hashtable();
				for ( int j=0;j< dblist.length;j++){
					htTemp.put(dblist[j].toString(), "");
				}
					Enumeration en = htDBConn.keys();
					while (en.hasMoreElements()){
						String tmp=(String)en.nextElement();
						if (!htTemp.containsKey(tmp)){
							logger.warn("Database Removed: removing database "+tmp);
							htDBConn.get(tmp).close();
							htDBConn.remove(tmp);
						
					}
					
				}
			}
			/**
			 * remove null or wrong connection			
			 */
			Enumeration en = htDBConn.keys() ;
			ArrayList alDBList =  new ArrayList();
			 while (en.hasMoreElements()){
				 alDBList.add((String)en.nextElement());
			 }
			String[] newDBList = (String[]) alDBList.toArray(new String [0]);
			
			for (int i=0; i<newDBList.length ;i++){
				
				
				SharedPoolDataSource spds = 
					(SharedPoolDataSource) htDBConn.get(newDBList[i]);
				
				logger.debug("retrieve connection for dbname ->"+newDBList[i]);
				//System.out.println("retrieve connection for dbname ->"+newDBList[i]);
			try {
				Connection con =spds.getConnection();
				logger.debug("sharedpooldatasource idle connection -->"+spds.getNumIdle()+" active connetion -->"+spds.getNumActive()+""+" dbname -->"+newDBList[i]);
				logger.debug("Starting ZabbixTrapper for "+newDBList[i]);
				final ZabbixTrapper trapper = c.getTrapper(newDBList[i]);
				Runnable runner = new dbJob(con,q, trapper,newDBList[i] );
				executor.execute(runner);
				
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


