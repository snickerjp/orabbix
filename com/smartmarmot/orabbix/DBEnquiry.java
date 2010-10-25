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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Level;

public class DBEnquiry {
	public static ZabbixItem[] execute(Query[] _queries, Connection _conn,
			String dbname) {
		if (_queries == null || _queries.length < 1) {
			throw new IllegalArgumentException("Query's array is empty or null");
		}
		Connection con = _conn;

		Collection<ZabbixItem> SZItems = new ArrayList<ZabbixItem>();
		ResultSet rs = null;
		PreparedStatement p_stmt = null;

		// System.out.println( " db : " + _dbConn.getName() );

		for (int i = 0; i < _queries.length; i++) {
			// System.out.println(queries[i].getSQL());
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			String datetime = dateFormat.format(_queries[i].getNextrun());

			// Configurator.logThis(Level.DEBUG,"Actual query is "+_queries[i].getName()+" on database="+
			// dbname+" Period="+_queries[i].getPeriod()+" nextRun="+datetime);
			// System.out.println(queries[i].getName());
			String tempStr = new String("");
			// check if is the right time to execute the statements
			try {

				if (_queries[i].getActive()) {
					// if item is active
					Date now = new Date(System.currentTimeMillis());
					Date nextRun = _queries[i].getNextrun();
					if (now.after(nextRun)) {

						Date newNextRun = new Date(nextRun.getTime()
								+ (_queries[i].getPeriod() * 1000 * 60));
						_queries[i].setNextrun(newNextRun);

						datetime = dateFormat.format(_queries[i].getNextrun());
						Configurator.logThis(Level.DEBUG, "Actual query is "
								+ _queries[i].getName() + "Nextrun " + datetime
								+ " on database=" + dbname + " Period="
								+ _queries[i].getPeriod());
						/*
						 * DateFormat dateFormat = new
						 * SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); String
						 * datetime = dateFormat.format(newNextRun);
						 * Configurator.logThis(Level.INFO
						 * ,"Query "+_queries[i].getName()+" on database="+
						 * dbname+" nextRun -->"+datetime);
						 */
						p_stmt = con.prepareStatement(_queries[i].getSQL()
								.toString());
						rs = p_stmt.executeQuery();
						ResultSetMetaData rsmd = rs.getMetaData();
						int numColumns = rsmd.getColumnCount();
						while (rs.next()) {
							// System.out.println(_queries[i].getSQL());
							// System.out.println(_queries[i].getName());
							// tempStr=rs.getObject(1).toString().trim();
							for (int r = 1; r < numColumns + 1; r++) {
								tempStr = tempStr
										+ rs.getObject(r).toString().trim();
							}
							Configurator.logThis(Level.INFO,
									"resultset returned from query "
											+ _queries[i].getName()
											+ " on database=" + dbname
											+ " resultset -->"
											+ tempStr.toString());
						}
						if (tempStr == null) {
							if (_queries[i].getNoData().length() > 0
									&& _queries[i].getNoData() != null) {
								tempStr = _queries[i].getNoData();
							}
						} else if (tempStr.length() == 0) {
							if (_queries[i].getNoData().length() > 0
									&& _queries[i].getNoData() != null) {
								tempStr = _queries[i].getNoData();
							}
						}
						ZabbixItem zitem = new ZabbixItem(
								_queries[i].getName(), tempStr);
						SZItems.add(zitem);
						Configurator.logThis(Level.WARN, "I'm going to return "
								+ tempStr + " for query "
								+ _queries[i].getName() + " on database="
								+ dbname);
					}

				}
			} catch (Exception ex) {
				Configurator.logThis(Level.ERROR,
						"Error on DBEnquiry on query=" + _queries[i].getName()
								+ " on database=" + dbname
								+ " Error returned is " + ex);
				if (_queries[i].getNoData().length() > 0
						&& _queries[i].getNoData() != null) {
					tempStr = _queries[i].getNoData();
				} else {
					tempStr = "";

				}
				ZabbixItem zitem = new ZabbixItem(_queries[i].getName(),
						tempStr);
				SZItems.add(zitem);
				Configurator.logThis(Level.WARN, "I'm going to return "
						+ tempStr + " for query " + _queries[i].getName()
						+ " on database=" + dbname);
			}

		}

		try {
			if (rs != null)
				rs.close();
		} catch (Exception ex) {
			Configurator.logThis(Level.ERROR,
					"Error on DBEnquiry while closing resultset "
							+ ex.getMessage() + " on database=" + dbname);
		}
		try {
			if (!con.isClosed())
				con.close();
		} catch (Exception ex) {
			Configurator.logThis(Level.ERROR,
					"Error on DBEnquiry while closing connection "
							+ ex.getMessage() + " on database=" + dbname);
		}
		ZabbixItem[] items = (ZabbixItem[]) SZItems.toArray(new ZabbixItem[0]);
		return items;
	}
}
