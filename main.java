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
 
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Level;
import com.smartmarmot.orabbix.Configurator;
import com.smartmarmot.orabbix.Constants;
import com.smartmarmot.orabbix.DBConn;
import com.smartmarmot.orabbix.DBJob;
import com.smartmarmot.orabbix.Item;
import com.smartmarmot.orabbix.Query;
import com.smartmarmot.orabbix.Sender;
import com.smartmarmot.orabbix.Utility;



public class main {
	public static final String Version="Version 1.0.2";
	public static final String Banner =Constants.PROJECT_NAME+" "+Version; 
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
            Configurator.logThis(Level.ALL,"Starting "+ Banner);
            
            
			String configFile;
			configFile= new String(args[0].toString());

			Configurator cfg =new Configurator(configFile);
			Integer maxThread=cfg.getMaxThread();

			ExecutorService executor = 
	               Executors.newFixedThreadPool(maxThread.intValue());
			
			RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
			String pid=rmxb.getName();
			Configurator.logThis(Level.ALL,"Orabbix started with pid:"+pid.split("@")[0].toString());
			
		//	System.out.print("pid: "+pid.split("@")[0].toString());
		
			String pidfile=cfg.getPidFile();
			try {
				Utility.writePid(pid.split("@")[0].toString(), pidfile);
			}catch (Exception e){
				Configurator.logThis(Level.ERROR,"Error while trying to write pidfile "+e);
			}
			
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
				Query[] q =c.getQueries();
			
			for (int i=0; i<dblist.length ;i++){
				if (!htDBConn.containsKey(dblist[i].toString())) {
					Configurator.logThis(Level.WARN,"New Database Founded: adding database "+dblist[i].toString());
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
				Hashtable<String, String> htTemp= new Hashtable<String, String>();
				for ( int j=0;j< dblist.length;j++){
					htTemp.put(dblist[j].toString(), "");
				}
					Enumeration<String> en = htDBConn.keys();
					while (en.hasMoreElements()){
						String tmp=(String)en.nextElement();
						if (!htTemp.containsKey(tmp)){
							Configurator.logThis(Level.WARN,"Database Removed: removing database "+tmp);
							htDBConn.get(tmp).close();
							htDBConn.remove(tmp);
						
					}
					
				}
			}
			/**
			 * remove null or wrong connection			
			 */
			Enumeration<String> en = htDBConn.keys() ;
			ArrayList<String> alDBList =  new ArrayList<String>();
			 while (en.hasMoreElements()){
				 alDBList.add((String)en.nextElement());
			 }
			String[] newDBList = (String[]) alDBList.toArray(new String [0]);
			
			for (int i=0; i<newDBList.length ;i++){
				
				
				SharedPoolDataSource spds = 
					(SharedPoolDataSource) htDBConn.get(newDBList[i]);
				
				Configurator.logThis(Level.DEBUG,"Retrieve connection for dbname ->"+newDBList[i]);
				 boolean alive=false;	
				   Hashtable<String, Integer> zabbixServers=c.getZabbixServers();
				   
				   try {
					   Connection cn = spds.getConnection();
					   PreparedStatement p_stmt = null;
					   p_stmt  = cn.prepareStatement("select sysdate from dual");
				       ResultSet rs = null;
				       rs = p_stmt.executeQuery();
				       rs.next();   
				       cn.close();
				       cn = null;						
				       BlockingQueue<Item> _queue = new LinkedBlockingQueue<Item>();
					   _queue.offer(new Item("alive", "1"));
					   _queue.offer(new Item("OrabbixVersion", Banner));
					   Sender sender = new Sender(_queue,zabbixServers, myDBConn[i].getName());
				       sender.run();
					   alive=true;
					   Configurator.logThis(Level.DEBUG,"Database "+myDBConn[i].getName()+" is alive");
					}catch (Exception ex){
						   BlockingQueue<Item> _queue = new LinkedBlockingQueue<Item>();
						   _queue.offer(new Item("alive", "0"));
						   Sender sender = new Sender(_queue,zabbixServers, myDBConn[i].getName());
					       sender.run();
					       Configurator.logThis(Level.ERROR,"Error diuring alive testing for dbname ->"+myDBConn[i].getName()+ex);
					}
					
					if (alive){
				//System.out.println("retrieve connection for dbname ->"+newDBList[i]);
							try {
								Locale.setDefault( Locale.US );
								Connection con =spds.getConnection();
								Configurator.logThis(Level.DEBUG,"sharedpooldatasource idle connection -->"+spds.getNumIdle()+" active connetion -->"+spds.getNumActive()+""+" dbname -->"+newDBList[i]);
								Configurator.logThis(Level.DEBUG,"Starting ZabbixTrapper for "+newDBList[i]);
								//final ZabbixTrapper trapper = c.getTrapper(newDBList[i]);
								Runnable runner = new DBJob(con,q,Constants.QUERY_LIST, zabbixServers,myDBConn[i].getName());
								executor.execute(runner);
								
							}catch (Exception e){
								
								Configurator.logThis(Level.ERROR,"Error in main while retrieve the connection for database "+ newDBList[i] +" error:  " + e);
							}
					}else{ //if database is become unreachable i'll send noDataFound 
						 BlockingQueue<Item> _queue = new LinkedBlockingQueue<Item>();
						 for ( int cnt=0; cnt < q.length; cnt++) {
							 _queue.offer(new Item(q[cnt].getName(),q[cnt].getNoData()));
						 }
						 Sender sender = new Sender(_queue,zabbixServers, myDBConn[i].getName());
				    	 sender.run();
						 }
			}
			
			Configurator.logThis(Level.DEBUG,"going in bed...and sleep for "+Configurator.getSleep()*1000+ " ms");
			//System.out.println("going in bed...and sleep for "+c.getSleep()*1000/60000+ " m");
			Thread.sleep(Configurator.getSleep()*1000);
			Configurator.logThis(Level.DEBUG,"waking up Goood Morning");
			
			}
			 
		 }catch (Exception ex){
			 ex.printStackTrace();
		 }
           
	}
	

}