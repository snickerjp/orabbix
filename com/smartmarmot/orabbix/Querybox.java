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
