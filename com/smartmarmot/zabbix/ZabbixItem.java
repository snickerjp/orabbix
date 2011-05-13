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

package com.smartmarmot.zabbix;

public final class ZabbixItem {

	private final String key;
	private final String value;
	private final String hostname;

	/**
	 * Create a literal value item.
	 * 
	 * @param key
	 *            The monitoring server's key for this statistic.
	 * @param value
	 *            The literal value.
	 */

	/*public ZabbixItem(final String key, final String value) {
		if (key == null || "".equals(key.trim())) {
			throw new IllegalArgumentException("empty key");
		}
		if (value == null) {
			throw new IllegalArgumentException("null value for key '" + key
					+ "'");
		}

		this.key = key;
		this.value = value;
		this.hostname = null;
	}
*/
	public ZabbixItem(final String key, final String value,
			final String hostname) {
		if (key == null || "".equals(key.trim())) {
			throw new IllegalArgumentException("empty key");
		}
		if (value == null) {
			throw new IllegalArgumentException("null value for key '" + key
					+ "'");
		}
		if (hostname == null) {
			throw new IllegalArgumentException("null value for hostname '"
					+ hostname + "'");
		}

		this.key = key;
		this.value = value;
		this.hostname = hostname;
	}

	/**
	 * @return The current hostname for this item.
	 * @throws Exception
	 */
	public String getHostName() throws Exception {
		return hostname;
		// return JMXHelper.Query(value, attribute);
	}

	/**
	 * Find the item's key.
	 * 
	 * @return The monitoring server's key for this item.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return The current value for this item.
	 * @throws Exception
	 *             When the item could not be queried in the platform's mbean
	 *             server.
	 */
	public String getValue() throws Exception {
		return value;
		// return JMXHelper.Query(value, attribute);
	}

}
