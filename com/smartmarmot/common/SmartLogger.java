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
