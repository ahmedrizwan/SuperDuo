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
import java.util.Vector;

public class MediaRule {

	Set mediaList;

	Vector statements = new Vector();

	public MediaRule(Set mediaList) {
		this.mediaList = mediaList;
	}

	public void add(Object rule) {
		statements.add(rule);
	}

	public void serialize(PrintWriter out) {
		out.print("@media ");
		String sep = "";
		Iterator it = mediaList.iterator();
		while(it.hasNext()) {
			out.print(sep);
			out.print(it.next());
			sep = ", ";
		}
		out.println(" {");
		Iterator list = statements.iterator();
		while (list.hasNext()) {
			Object stmt = list.next();
			if (stmt instanceof BaseRule) {
				((SelectorRule) stmt).serialize(out);
				out.println();
			} else if (stmt instanceof MediaRule) {
				((MediaRule) stmt).serialize(out);
				out.println();
			}
		}
		out.println("}");
	}

}
