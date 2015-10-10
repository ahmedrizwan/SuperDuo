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
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public abstract class BaseRule {

	TreeMap properties;

	BaseRule() {
		properties = new TreeMap();
	}

	BaseRule(TreeMap props) {
		this.properties = props;
	}

	protected BaseRule(BaseRule other) {
		properties = new TreeMap();
		Iterator it = other.properties.entrySet().iterator();
		while( it.hasNext() ) {
			Map.Entry entry = (Map.Entry)it.next();
			// not cloning CSS values
			properties.put(entry.getKey(), entry.getValue());
		}
		
	}
	
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public CSSValue get(String property) {
		return (CSSValue)properties.get(property);
	}

	public void set(String property, CSSValue value) {
		if (value == null)
			properties.remove(property);
		else
			properties.put(property, value);
	}

	public Iterator properties() {
		return properties.keySet().iterator();
	}

	public abstract void serialize(PrintWriter out);

	public void serializeProperties(PrintWriter out, boolean newlines) {
		Iterator entries = properties.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			if (newlines)
				out.print('\t');
			out.print(entry.getKey());
			out.print(": ");
			((CSSValue)entry.getValue()).serialize(out);
			out.print(";");
			if (newlines)
				out.println();
			else
				out.print(' ');
		}
	}

	public String toString() {
		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);
		serialize(pw);
		pw.flush();
		return out.toString();
	}
}
