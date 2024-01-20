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

package com.smartmarmot.common;

import org.apache.log4j.Level;

import org.apache.log4j.Logger;

import com.smartmarmot.orabbix.Constants;


public class SmartLogger {
	public static void logThis(Level level, String message) {
		Logger  logger = Logger.getLogger(Constants.PROJECT_NAME);
		if (message == null) {
			message = new String("");
		}
		logger.log(level, message);
	}

}
