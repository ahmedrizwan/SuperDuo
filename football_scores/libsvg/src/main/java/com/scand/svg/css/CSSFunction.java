/*
	SVG Kit for Android library
    Copyright (C) 2015 SCAND Ltd, svg@scand.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.scand.svg.css;

import java.io.PrintWriter;

public class CSSFunction extends CSSValue {

	private final String ident;
	private final CSSValue[] params;
	
	CSSFunction( String ident, CSSValue[] params ) {
		this.ident = ident;
		this.params = params;
	}
	
	public void serialize(PrintWriter out) {
		out.print(ident);
		out.print('(');
		String sep = "";
		for( int i = 0 ; i < params.length ; i++ ) {
			out.print(sep);
			params[i].serialize(out);
			sep = ", ";
		}
		out.print(')');
	}
	
	public boolean equals(Object other) {
		if( this == other )
			return true;
		if (other.getClass() != getClass())
			return false;
		CSSFunction o = (CSSFunction) other;
		if (!o.ident.equals(ident) || o.params.length != params.length)
			return false;
		for (int i = 0; i < params.length; i++) {
			if (!params[i].equals(o.params[i]))
				return false;
		}
		return true;
	}

	public int hashCode() {
		int code = ident.hashCode();
		for (int i = 0; i < params.length; i++) {
			code += (i+2) * params[i].hashCode();
		}
		return code;
	}	
}
