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

public class CSSImportant extends CSSValue {

	private final CSSValue value;

	CSSImportant(CSSValue value) {
		this.value = value;
	}

	public void serialize(PrintWriter out) {
		value.serialize(out);
		out.print(" !important");
	}

	public Object getValue() {
		return value;
	}

	public int hashCode() {
		return value.hashCode() + 42;
	}

	public boolean equals(Object other) {
		if (other.getClass() == getClass()) {
			CSSImportant o = (CSSImportant) other;
			return o.value.equals(value);
		}
		return false;
	}
}
