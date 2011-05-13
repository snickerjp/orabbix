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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Level;

import com.smartmarmot.common.SmartLogger;

public class Utility {

	public static void writePid(String _pid, String _pidfile) throws Exception {
		try {

			// Open an output stream

			File target = new File(_pidfile);

			File newTarget = new File(target.getAbsoluteFile()
					.getCanonicalPath());
			target = null;

			if (newTarget.exists()) {
				boolean success = newTarget.delete();
				if (!success) {
					SmartLogger.logThis(Level.ERROR,
							"Delete: deletion failed "
									+ newTarget.getAbsolutePath());
				}
			}
			if (!newTarget.exists()) {
				FileOutputStream fout = new FileOutputStream(newTarget);
				new PrintStream(fout).print(_pid);
				// Close our output stream
				fout.close();
			}

		}
		// Catches any error conditions
		catch (IOException e) {
			SmartLogger.logThis(Level.ERROR, "Unable to write to file "
					+ _pidfile + " error:" + e);
		}

	}
}
