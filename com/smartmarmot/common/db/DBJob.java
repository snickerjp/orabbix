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
 * orabbix. If not, see <http://www.gnu.org/licenses/>.
 */
package com.smartmarmot.common.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.dbcp.datasources.SharedPoolDataSource;
import org.apache.log4j.Level;

import com.smartmarmot.orabbix.Configurator;
import com.smartmarmot.orabbix.Constants;
import com.smartmarmot.orabbix.Query;
import com.smartmarmot.orabbix.Sender;
import com.smartmarmot.zabbix.ZabbixItem;

public class DBJob implements Runnable {
	private final SharedPoolDataSource _spds;
	private Query[] _queries;
	private final BlockingQueue<ZabbixItem> _queue = new LinkedBlockingQueue<ZabbixItem>();
	private final String _dbname;
	private final String _queriesGroup;
	private final int _dgNum;
	private final Hashtable<String, Integer> _zabbixServers;

	public DBJob(SharedPoolDataSource spds, Query[] queries, String queriesGroup,
			Hashtable<String, Integer> zabbixServers, String dbname) {
		this._spds = spds;
		this._queries = queries;
		this._queriesGroup = queriesGroup;
		this._zabbixServers = zabbixServers;
		this._dbname = dbname;
		this._dgNum = 0;
	}

	public DBJob(SharedPoolDataSource spds, Query[] queries, String queriesGroup,
			Hashtable<String, Integer> zabbixServers, String dbname, int dgNum) {
		this._spds = spds;
		this._queries = queries;
		this._zabbixServers = zabbixServers;
		this._queriesGroup = queriesGroup;
		this._dbname = dbname;
		if (dgNum > 0) {
			this._dgNum = dgNum;
		} else {
			this._dgNum = 0;
		}
	}

	private boolean Alive(Connection _conn){
		try {
			PreparedStatement p_stmt = null;
			p_stmt = _conn
			.prepareStatement(Constants.ORACLE_VALIDATION_QUERY);
			ResultSet rs = null;
			rs = p_stmt.executeQuery();
			rs.next();
			//_conn.close();
			BlockingQueue<ZabbixItem> _queue = new LinkedBlockingQueue<ZabbixItem>();
			_queue.offer(new ZabbixItem("alive", "1",this._dbname));
			_queue.offer(new ZabbixItem(Constants.PROJECT_NAME+"Version", Constants.BANNER,this._dbname));
			Sender sender = new Sender(_queue, this._zabbixServers,
					this._dbname);
			sender.run();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Configurator.logThis(Level.DEBUG, "Database "
					+ this._dbname + " is not alive");
			return false;
		}
	}

	public void run() {

		Configurator.logThis(Level.DEBUG, "Starting dbJob on database "
				+ _dbname + " " + _queriesGroup);
		final long start = System.currentTimeMillis();

		try {
			Connection dbConn=this._spds.getConnection();
/*			if (dbConn.isClosed()){
				dbConn = this._spds.getConnection();
			}*/
			if (Alive(dbConn)){
				ZabbixItem[] zitems = DBEnquiry.execute(this._queries,
						dbConn, this._dbname);
				if (zitems != null && zitems.length > 0) {
					Configurator.logThis(Level.DEBUG, "Item retrieved "
							+ zitems.length + " on database " + this._dbname);
					for (int cnt = 0; cnt < zitems.length; cnt++) {
						String zItemName = zitems[cnt].getKey();
						if (this._dgNum > 0) {
							zItemName = zItemName + "_" + _dgNum;
						}
						Configurator.logThis(Level.DEBUG, "dbname " + this._dbname
								+ " sending item  " + zitems[cnt].getKey()
								+ " value " + zitems[cnt].getValue());
						_queue.offer(new ZabbixItem(zItemName, zitems[cnt]
						                                              .getValue(),
						                                              _dbname));
					}
					Sender sender = new Sender(_queue, _zabbixServers, this._dbname);
					sender.run();
				}
				dbConn.close();
			} else{
				BlockingQueue<ZabbixItem> _queue = new LinkedBlockingQueue<ZabbixItem>();
				_queue.offer(new ZabbixItem("alive", "0",this._dbname));
				_queue.offer(new ZabbixItem(Constants.PROJECT_NAME+"Version", Constants.BANNER,this._dbname));
				for (int cnt = 0; cnt < this._queries.length; cnt++) {
					_queue.offer(new ZabbixItem(_queries[cnt].getName(),
							_queries[cnt].getNoData(),_dbname));
					
				}
				Sender sender = new Sender(_queue, _zabbixServers,
						_dbname);
				sender.run();
			}
			} catch (Exception e) {
				Configurator.logThis(Level.ERROR, "Error on dbJob for database "
						+ _dbname + " " + _queriesGroup + " error: " + e);
			} finally {
				if (_queries != null)
					_queries = null;
			}
			Configurator.logThis(Level.INFO, "Done with dbJob on database "
					+ _dbname + " " + _queriesGroup + " elapsed time "
					+ (System.currentTimeMillis() - start) + " ms");
		}
	}
