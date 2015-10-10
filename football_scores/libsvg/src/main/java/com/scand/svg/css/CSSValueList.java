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

public class CSSValueList extends CSSValue {

	char separator;

	CSSValue[] values;

	public CSSValueList(char sep, CSSValue[] vals) {
		this.separator = sep;
		this.values = vals;
	}

	public char getSeparator() {
		return separator;
	}

	public int length() {
		return values.length;
	}

	public CSSValue item(int i) {
		return values[i];
	}

	public void serialize(PrintWriter out) {
		String sep = "";
		for (int i = 0; i < values.length; i++) {
			out.print(sep);
			values[i].serialize(out);
			if (separator == ' ')
				sep = " ";
			else
				sep = separator + " ";
		}
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other.getClass() != getClass())
			return false;
		CSSValueList o = (CSSValueList) other;
		if (o.separator != separator || o.values.length != values.length)
			return false;
		for (int i = 0; i < values.length; i++) {
			if (!values[i].equals(o.values[i]))
				return false;
		}
		return true;
	}

	public int hashCode() {
		int code = separator;
		for (int i = 0; i < values.length; i++) {
			code += (i + 1) * values[i].hashCode();
		}
		return code;
	}

	public static int valueCount(Object value, char op) {
		if (value instanceof CSSValueList) {
			CSSValueList vl = (CSSValueList) value;
			if (vl.separator == op)
				return vl.values.length;
		}
		return 1;
	}

	public static Object valueAt(Object value, int index, char op) {
		if (value instanceof CSSValueList) {
			CSSValueList vl = (CSSValueList) value;
			if (vl.separator == op)
				return vl.values[index];
		}
		if (index == 0)
			return value;
		throw new ArrayIndexOutOfBoundsException(index);
	}
}
