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

import org.apache.log4j.Level;

public class Querybox {
	private Query[] _query;
	private String _dbname;
	private String _queriesfile;

	public Querybox(String _localdbname, String _prp) {
		try {
			setQueries(Configurator.getQueries(Configurator
					.getPropsFromFile(_prp)));
			this.setQueriesFile(_prp);

			if (_localdbname.length() > 0 && _localdbname != null) {
				this.setDBName(_localdbname);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Configurator.logThis(Level.ERROR, "Error on QueryBox "
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

	public void refresh() {
		Query queryTmp[] = this.getQueries();
		this.setQueries(Configurator.refresh(queryTmp, _queriesfile));
	}

	public void setDBName(String _dbname) {
		this._dbname = _dbname;
	}

	public void setQueries(Query[] _query) {
		this._query = _query;
	}

	public void setQueriesFile(String _queriesfile) {
		this._queriesfile = _queriesfile;
	}

}
