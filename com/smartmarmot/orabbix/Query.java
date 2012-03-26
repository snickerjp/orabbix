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


import java.util.List;
import java.util.Date;

public class Query {

	private String sql;
	private String name;
	private String nodata;
	private String racequery;
	private String racevalue;
	private List<Integer> raceExcludeColumns = null;
	private int period = -1;
	private Date nextrun;
	private Boolean active = true;
	private List<Integer> excludeColumns = null;
	private Boolean space = false;
	private Boolean trim = true;

	public Query(String _query, String _name, String _nodata, String _rccondq,
			String _rccondval, int _period, Boolean _active, Boolean _trim,
			Boolean _space, List<Integer> _excludeColumns,
			List<Integer> _raceExcludeColumns) {
		if (_query == null || _query.length() == 0)
			throw new RuntimeException("empty query");
		this.sql = _query;
		if (_name != null && _name.length() > 0)
			this.name = _name;
		else
			this.name = _query;
		if (_nodata != null && _nodata.length() > 0) {
			this.nodata = _nodata;
		} else
			this.nodata = "";
		this.racequery = "";
		this.racevalue = "";
		if (_rccondq != null && _rccondq.length() > 0) {
			if (_rccondval != null && _rccondval.length() > 0) {
				this.racequery = _rccondq;
				this.racevalue = _rccondval;
			}
		}
		if (_period != 0) {
			this.period = _period;
			if (period > 0) {
				Date newNextRun = new Date(System.currentTimeMillis());
				// Date now =new Date(System.currentTimeMillis());
				// Date newNextRun=new Date(now.getTime()+(period*1000*60));
				this.nextrun = newNextRun;
			}
		}
		if (_trim != null) {
			this.setTrim(_trim);
		}
		if (_space != null) {
			this.setSpace(_space);
		}
		if (_active != null) {
			this.setActive(_active);
		}
		if (_excludeColumns != null) {
			this.setExcludeColumnsList(_excludeColumns);
		}
		if (_raceExcludeColumns != null) {
			this.setRaceExcludeColumnsList(_raceExcludeColumns);
		}
	}

	public Boolean getActive() {
		return active.booleanValue();
	}

	public String getName() {
		return this.name;
	}

	public Date getNextrun() {
		return nextrun;
	}

	public String getNoData() {
		return this.nodata;
	}

	public int getPeriod() {
		return period;
	}

	public String getRaceQuery() {
		return this.racequery;
	}

	public String getRaceValue() {
		return this.racevalue;
	}

	public String getSQL() {
		return this.sql;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setNextrun(Date nextrun) {
		this.nextrun = nextrun;
	}

	public void setPeriod(int period) {
		this.period = period;

	}

	public void setRaceQuery(String _raceQuery) {
		this.racequery = _raceQuery;
	}

	public void setSql(String _sql) {
		this.sql = _sql;
		// TODO Auto-generated method stub

	}

	public void setTrim(Boolean trim) {
		this.trim = trim;
	}
	public Boolean getTrim() {
		return trim;
	}
	public void setSpace(Boolean space) {
		this.space = space;
	}
	public Boolean getSpace() {
		return space;
	}
	public List<Integer> getExcludeColumnsList() {
		return this.excludeColumns;
	}
	public void setExcludeColumnsList(List<Integer> excludeList) {
		if (excludeList != null) {
			this.excludeColumns = excludeList;
		}
	}
	public void setRaceExcludeColumnsList(List<Integer> raceExcludeColumns) {
		this.raceExcludeColumns = raceExcludeColumns;
	}
	public List<Integer> getRaceExcludeColumnsList() {
		return raceExcludeColumns;
	}
}