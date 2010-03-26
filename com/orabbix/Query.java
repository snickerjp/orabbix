/*
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

package com.orabbix;

public class Query {
  
  private String sql;
  private String name;
  private String nodata;
  
  public Query( String _query, String _name, String _nodata) {
    if( _query == null || _query.length() ==0)
      throw new RuntimeException( "empty query");
    this.sql    = _query;
    if ( _name != null && _name.length() >0 )
      this.name = _name;
    else
      this.name = _query;
   if ( _nodata != null && _nodata.length() >0 ){
	   this.nodata=_nodata;   
   }else 
	   this.nodata="";
    
  }
  
  public String getName() {
    return this.name;
  }
  public String getSQL() {
    return this.sql;
  }
  
  public String getNoData() {
	return this.nodata;
  }

}