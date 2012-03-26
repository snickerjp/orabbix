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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Level;

import com.smartmarmot.common.SmartLogger;
import com.smartmarmot.orabbix.Configurator;

public class Querybox {
	private Query[] _query;
	private String _dbname;
	private String _queriesfile;
	private String _extraqueriesfile;

	public Querybox(String _localdbname, String _prp,String _extraprp) {
		try {
			if (Configurator.propVerify(_prp)){
				this.setQueriesFile(_prp);
			}
			if (Configurator.propVerify(_extraprp)){
				this.setExtraQueriesFile(_extraprp);
			}
			
			if (_localdbname.length() > 0 && _localdbname != null) {
				this.setDBName(_localdbname);
			}
		refresh();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			SmartLogger.logThis(Level.ERROR, "Error on QueryBox "
					+ e.getMessage());
		}

	}

	public String getDBName() {
		return _dbname;
	}

	public Query[] getQueries() {
		return _query;
	}
	
	
	public String getQueriesFile() {
		return _queriesfile;
	}
	

	public void refresh()  {
		try {
			Query[] firstq=null;
			Query[] extraq=null;
			
			if (_queriesfile != null){
				Properties prp = Configurator.getPropsFromFile(_queriesfile);
				firstq =Configurator.getQueries(prp);
				//this.setQueries(q);
			}
			if (_extraqueriesfile != null){
				Properties prp = Configurator.getPropsFromFile(_extraqueriesfile);
				extraq =Configurator.getQueries(prp);
			}
		
			Query[] refreshedq = addQueries(firstq,extraq);
			if (this._query==null){
				this.setQueries(refreshedq);
			} else {
				for (int i = 0; i <refreshedq.length; i++){
					for (int j = 0; j <  this._query.length; j++){
						if (_query[j].getName().equals(refreshedq[i].getName())){
							refreshedq[i].setNextrun(_query[j].getNextrun());
						}
					}
				}
				this.setQueries(refreshedq);
			}	
			
			
			
		}catch (Exception ex){
			SmartLogger.logThis(Level.ERROR, "Error on Querybox "+ex);
			
		}
		
	
	}

	public void setDBName(String _dbname) {
		this._dbname = _dbname;
	}

	public void setQueries(Query[] _query) {
		this._query = null;
		this._query = _query;
	}

	public Query[] addQueries(Query[] _originalquery,Query[] _newquery) {
		ArrayList<Query> tempOriginalArray = new ArrayList<Query>(Arrays.asList(_originalquery));
		ArrayList<Query> tempNewArray = new ArrayList<Query>(Arrays.asList(_newquery));
		for (int i = 0; i < _originalquery.length; i++){
			for (int j = 0; j < _newquery.length; j++){
				if (_originalquery[i].getName().equals(_newquery[j].getName())){
					tempOriginalArray.remove(_originalquery[i]);
					tempOriginalArray.add(_newquery[j]);
					tempNewArray.remove(_newquery[j]);
				}
			} 	
		}
		tempOriginalArray.addAll(tempNewArray);
		Query[] queries = (Query[]) tempOriginalArray.toArray(new Query[0]);
		//this.setQueries(queries);
		return queries;
	}

	public void setQueriesFile(String _queriesfile) {
		this._queriesfile = _queriesfile;
	}
	
	public void setExtraQueriesFile(String _extraqueriesfile) {
		this._extraqueriesfile = _extraqueriesfile;
	}

}
