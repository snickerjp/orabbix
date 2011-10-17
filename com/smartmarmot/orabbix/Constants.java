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

package com.smartmarmot.orabbix;


public class Constants {
	public static final String VERSION = "Version 1.2.2 ";
	public static final String BANNER = Constants.PROJECT_NAME + " " + VERSION;
	public static final String PROJECT_NAME = "Orabbix";
	public static final String DATABASES_LIST = "DatabaseList";
	public static final String DELIMITER = ",";
	public static final String QUERY_LIST = "QueryList";
	public static final String QUERY_LIST_FILE = "QueryListFile";
	public static final String EXTRA_QUERY_LIST_FILE = "ExtraQueryListFile";
	public static final String QUERY_POSTFIX = "Query";
	public static final String QUERY_NO_DATA_FOUND = "NoDataFound";
	public static final String QUERY_WHEN_NOT_ALIVE = "WhenNotAlive";
	public static final String CONN_URL = "Url";
	public static final String CONN_USERNAME = "User";
	public static final String CONN_PASSWORD = "Password";
	public static final String CONN_DEFAULT_USERNAME = "DefaultUser";
	public static final String CONN_DEFAULT_PASSWORD = "DefaultPassword";
	public static final String CONN_MAX_ACTIVE = "MaxActive";
	public static final String CONN_MAX_IDLE = "MaxIdle";
	public static final String CONN_MAX_WAIT = "MaxWait";
	public static final String ORACLE = "Oracle";
	public static final String ORACLE_VALIDATION_QUERY = "SELECT SYSDATE FROM DUAL";
	public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	public static final String ORACLE_WHOAMI_QUERY = "SELECT SYS_CONTEXT ('USERENV', 'SESSION_USER') FROM DUAL";
	public static final String ORACLE_DBNAME_QUERY = "SELECT SYS_CONTEXT ('USERENV', 'DB_NAME') FROM DUAL";
	public static final String RACE_CONDITION_QUERY = "RaceConditionQuery";
	public static final String RACE_CONDITION_EXCLUDE_COLUMNS = "RaceConditionQueryExcludeColumnsList";
	public static final String RACE_CONDITION_VALUE = "RaceConditionValue";
	public static final String QUERY_PERIOD = "Period";
	public static final String QUERY_DEFAULT_PERIOD = "DefaultQueryPeriod";
	public static final String QUERY_ACTIVE = "Active";
	public static final String ZABBIX_SERVER_LIST = "ZabbixServerList";
	public static final String ZABBIX_SERVER_PORT = "Port";
	public static final String ZABBIX_SERVER_HOST = "Address";
	public static final String ORABBIX_PIDFILE = "OrabbixDaemon.PidFile";
	public static final String ORABBIX_DAEMON_SLEEP = "OrabbixDaemon.Sleep";
	public static final String ORABBIX_DAEMON_THREAD = "OrabbixDaemon.MaxThreadNumber";
	public static final int ZABBIX_SERVER_DEFAULT_PORT = 10051;
	public static final String QUERY_TRIM = "Trim";
	public static final String QUERY_SPACE = "AddSpaces";
	public static final String QUERY_EXCLUDE_COLUMNS = "ExcludeColumnsList";
}