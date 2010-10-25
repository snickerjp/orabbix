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
import com.smartmarmot.orabbix.Query;
import com.smartmarmot.orabbix.Querybox;
import com.smartmarmot.orabbix.Sender;
import com.smartmarmot.orabbix.Utility;
import com.smartmarmot.orabbix.ZabbixItem;

public class main {
	public static final String Version = "Version 1.1.0 RC1";
	public static final String Banner = Constants.PROJECT_NAME + " " + Version;

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
			Configurator.logThis(Level.ALL, "Starting " + Banner);

			String configFile;
			configFile = new String(args[0].toString());

			Configurator cfg = new Configurator(configFile);
			Integer maxThread = cfg.getMaxThread();

			ExecutorService executor = Executors.newFixedThreadPool(maxThread
					.intValue());

			RuntimeMXBean rmxb = ManagementFactory.getRuntimeMXBean();
			String pid = rmxb.getName();
			Configurator.logThis(Level.ALL, "Orabbix started with pid:"
					+ pid.split("@")[0].toString());

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

				
				if (!c.isEqualsDBList(myDBConn)) {
					/**
					 * rebuild connections DBConn[]
					 */
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

			

				for (int i = 0; i < myDBConn.length; i++) {
					Querybox actqb = qbox.get(myDBConn[i].getName());
					actqb.refresh();
					Query[] q = actqb.getQueries();

					SharedPoolDataSource spds = myDBConn[i].getSPDS();

					Configurator.logThis(Level.DEBUG,
							"Retrieve connection for dbname ->" + myDBConn[i]);
					boolean alive = false;
					Hashtable<String, Integer> zabbixServers = c
							.getZabbixServers();

					try {
						Connection cn = spds.getConnection();
						PreparedStatement p_stmt = null;
						p_stmt = cn
								.prepareStatement("select sysdate from dual");
						ResultSet rs = null;
						rs = p_stmt.executeQuery();
						rs.next();
						cn.close();
						cn = null;
						BlockingQueue<ZabbixItem> _queue = new LinkedBlockingQueue<ZabbixItem>();
						_queue.offer(new ZabbixItem("alive", "1"));
						_queue.offer(new ZabbixItem("OrabbixVersion", Banner));
						Sender sender = new Sender(_queue, zabbixServers,
								myDBConn[i].getName());
						sender.run();
						alive = true;
						Configurator.logThis(Level.DEBUG, "Database "
								+ myDBConn[i].getName() + " is alive");
					} catch (Exception ex) {
						BlockingQueue<ZabbixItem> _queue = new LinkedBlockingQueue<ZabbixItem>();
						_queue.offer(new ZabbixItem("alive", "0"));
						Sender sender = new Sender(_queue, zabbixServers,
								myDBConn[i].getName());
						sender.run();
						Configurator.logThis(Level.ERROR,
								"Error diuring alive testing for dbname ->"
										+ myDBConn[i].getName() + ex);
					}

					if (alive) {
						// System.out.println("retrieve connection for dbname ->"+newDBList[i]);
						try {
							Locale.setDefault(Locale.US);
							Connection con = spds.getConnection();
							Configurator.logThis(Level.DEBUG,
									"sharedpooldatasource idle connection -->"
											+ spds.getNumIdle()
											+ " active connetion -->"
											+ spds.getNumActive() + ""
											+ " dbname -->" + myDBConn[i]);
							Configurator
									.logThis(Level.DEBUG,
											"Starting ZabbixTrapper for "
													+ myDBConn[i]);
							// final ZabbixTrapper trapper =
							// c.getTrapper(newDBList[i]);
							Runnable runner = new DBJob(con, q,
									Constants.QUERY_LIST, zabbixServers,
									myDBConn[i].getName());
							executor.execute(runner);

						} catch (Exception e) {

							Configurator.logThis(Level.ERROR,
									"Error in main while retrieve the connection for database "
											+ myDBConn[i] + " error:  " + e);
						}
					} else { // if database is become unreachable i'll send
								// noDataFound
						BlockingQueue<ZabbixItem> _queue = new LinkedBlockingQueue<ZabbixItem>();
						for (int cnt = 0; cnt < q.length; cnt++) {
							_queue.offer(new ZabbixItem(q[cnt].getName(),
									q[cnt].getNoData()));
						}
						Sender sender = new Sender(_queue, zabbixServers,
								myDBConn[i].getName());
						sender.run();
					}
				}

				Configurator.logThis(Level.DEBUG,
						"going in bed...and sleep for "
								+ Configurator.getSleep() * 1000 + " ms");
				// System.out.println("going in bed...and sleep for "+c.getSleep()*1000/60000+
				// " m");
				Thread.sleep(Configurator.getSleep() * 1000);
				Configurator.logThis(Level.DEBUG, "waking up Goood Morning");

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