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
import java.util.Iterator;
import java.util.Set;

public class ImportRule {

	String url;

	Set mediaList;

	public ImportRule(String url, Set mediaList) {
		this.url = url;
		this.mediaList = mediaList;
	}

	public void serialize(PrintWriter out) {
		out.print("@import url(");
		out.print(url);
		out.print(")");
		if (mediaList != null) {
			Iterator it = mediaList.iterator();
			String sep = " ";
			while(it.hasNext()) {
				out.print(sep);
				out.print(it.next());
				sep = ", ";
			}
		}
		out.println(";");
	}

}
