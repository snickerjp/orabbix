package com.smartmarmot.orabbix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.Level;



public class Utility {

	public static void writePid(String _pid,String _pidfile) throws Exception {
		try
		{
			
		    // Open an output stream

			File target = new File(_pidfile);
			
			File newTarget = new File(target.getAbsoluteFile().getCanonicalPath());
			target=null;
			
			if (newTarget.exists()){
				boolean success = newTarget.delete();
				if (!success){
					Configurator.logThis(Level.ERROR,"Delete: deletion failed "+newTarget.getAbsolutePath());
				  }
			}
			if (!newTarget.exists()){
				FileOutputStream fout = new FileOutputStream (newTarget);
				new PrintStream(fout).print(_pid);
			    // Close our output stream
			    fout.close();		
			}			

		}
		// Catches any error conditions
		catch (IOException e)
		{
			Configurator.logThis(Level.ERROR,"Unable to write to file "+_pidfile+" error:"+e);
		}

	}
}
