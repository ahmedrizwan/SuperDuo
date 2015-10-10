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

public class AndSelector extends Selector {

	Selector first;

	Selector second;

	AndSelector(Selector first, Selector second) {
		this.first = first;
		this.second = second;
	}

	public ElementMatcher getElementMatcher() {
		return new AndElementMatcher(this, first.getElementMatcher(), second.getElementMatcher());
	}

	public int getSpecificity() {
		return addSpecificity(first.getSpecificity(), second.getSpecificity());
	}

	public void serialize(PrintWriter out) {
		first.serialize(out);
		second.serialize(out);
	}

	public boolean equals(Object other) {
		if( this == other )
			return true;
		if (other.getClass() != getClass())
			return false;
		AndSelector o = (AndSelector) other;
		return o.first.equals(first) && o.second.equals(second);
	}

	public int hashCode() {
		return 3*first.hashCode() + second.hashCode();
	}	
}
