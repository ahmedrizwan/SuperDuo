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

public class ClassSelector extends Selector {

	String className;

	ClassSelector(String className) {
		this.className = className;
	}

	public ElementMatcher getElementMatcher() {
		return new ClassElementMatcher(this, className);
	}

	public int getSpecificity() {
		return 0x100;
	}

	public void serialize(PrintWriter out) {
		out.print('.');
		out.print(className);
	}

	public boolean equals(Object arg) {
		if (getClass() != arg.getClass())
			return false;
		return className.equals(((ClassSelector) arg).className);
	}

	public int hashCode() {
		return className.hashCode();
	}

}
