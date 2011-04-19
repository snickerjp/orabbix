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
 *  . If not, see <http://www.gnu.org/licenses/>.
 */

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Level;

import com.smartmarmot.orabbix.Configurator;
import com.smartmarmot.orabbix.Constants;
import com.smartmarmot.orabbix.DBConn;
import com.smartmarmot.orabbix.DBJob;
import com.smartmarmot.orabbix.Query;
import com.smartmarmot.orabbix.Querybox;
import com.smartmarmot.orabbix.Utility;

public class main {
	
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
			Configurator.logThis(Level.INFO, "Starting " + Constants.BANNER);

			String configFile;
			configFile = new String(args[0].toString());

			Configurator cfg = new Configurator(configFile);
			
			RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
			String pid = rmxb.getName();
			Configurator.logThis(Level.INFO, Constants.PROJECT_NAME
					+ " started with pid:" + pid.split("@")[0].toString());
			// System.out.print("pid: "+pid.split("@")[0].toString());
			String pidfile = cfg.getPidFile();
			try {
				Utility.writePid(pid.split("@")[0].toString(), pidfile);
			} catch (Exception e) {
				Configurator.logThis(Level.ERROR,
						"Error while trying to write pidfile " + e);
			}

			Locale.setDefault(Locale.US);
			DBConn[] myDBConn = cfg.getConnections();
			if (myDBConn == null) {
				Configurator.logThis(Level.ERROR,
						"ERROR on main - Connections is empty or null");
			} else if (myDBConn.length == 0) {
				Configurator.logThis(Level.ERROR,
						"ERROR on main - Connections is empty or null");
			}
			/**
			 * retrieve maxThread
			 */
			Integer maxThread = 0;
			try {
				maxThread = cfg.getMaxThread();
			} catch (Exception e) {
				Configurator.logThis(Level.WARN,
						"MaxThread not defined calculated maxThread = "
								+ myDBConn.length * 3);
			}
			if (maxThread == null)
				maxThread = 0;
			if (maxThread == 0) {
				maxThread = myDBConn.length * 3;
			}
			
			ExecutorService executor = Executors.newFixedThreadPool(maxThread
					.intValue());
			/**
			 * populate qbox
			 */
			Hashtable<String, Querybox> qbox = new Hashtable<String, Querybox>();
			for (int i = 0; i < myDBConn.length; i++) {
				if (cfg.hasQueryFile(myDBConn[i].getName())) {
					String queryFile = cfg.getQueryFile(myDBConn[i].getName());
					Querybox qboxtmp = new Querybox(myDBConn[i].getName(),
							queryFile);
					qbox.put(myDBConn[i].getName(), qboxtmp);
				} else {
					String queryFile = cfg.getQueryFile();
					Querybox qboxtmp = new Querybox(myDBConn[i].getName(),
							queryFile);
					qbox.put(myDBConn[i].getName(), qboxtmp);
				}
			}

			cfg = null;
			/**
			 * daemon begin here
			 */
			while (true) {
				/**
				 * istantiate a new configurator
				 */
				Configurator c = new Configurator(configFile);

				/*
				 * here i rebuild DB's List
				 * */
				if (!c.isEqualsDBList(myDBConn)) {
					
					 // rebuild connections DBConn[]
					 
					myDBConn = c.rebuildDBList(myDBConn);
					for (int i = 1; i < myDBConn.length; i++) {
						if (!qbox.containsKey(myDBConn[i].getName())){
							if (c.hasQueryFile(myDBConn[i].getName())) {
								String queryFile = c.getQueryFile(myDBConn[i].getName());
								Querybox qboxtmp = new Querybox(myDBConn[i].getName(),
										queryFile);
								qbox.put(myDBConn[i].getName(), qboxtmp);
							} else {
								String queryFile = c.getQueryFile();
								Querybox qboxtmp = new Querybox(myDBConn[i].getName(),
										queryFile);
								qbox.put(myDBConn[i].getName(), qboxtmp);
							}
						}
					}
				}

			
				/*
				 * ready to run query
				 * 
				 * */
				
				for (int i = 0; i < myDBConn.length; i++) {
					Querybox actqb = qbox.get(myDBConn[i].getName());
					actqb.refresh();
					Query[] q = actqb.getQueries();

					SharedPoolDataSource spds = myDBConn[i].getSPDS();
					
					Hashtable<String, Integer> zabbixServers = c
							.getZabbixServers();
					Configurator.logThis(Level.DEBUG,
							"Ready to run DBJob for dbname ->"
									+ myDBConn[i].getName());
					Runnable runner = new DBJob(spds, q,
							Constants.QUERY_LIST, zabbixServers,
							myDBConn[i].getName());
					executor.execute(runner);
				
				}
				Thread.sleep(60*1000);
				Configurator.logThis(Level.DEBUG, "Waking up Goood Morning");

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void printUsage() {
		System.out.println("USAGE");
		System.out.println("run.sh -Dlog4j.properties=configfile");
		System.out.println("log4j.properties = log4j configuration file");
		System.out.println("configfile 		 = configuration parameter file");
		System.err.println("ARGS1 parameterfile");
	}

}