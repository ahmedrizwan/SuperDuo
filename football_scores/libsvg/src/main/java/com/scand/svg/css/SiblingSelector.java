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

public class SiblingSelector extends Selector {

	private Selector prev;

	private Selector curr;

	SiblingSelector(Selector prev, Selector curr) {
		this.prev = prev;
		this.curr = curr;
	}

	public ElementMatcher getElementMatcher() {
		return new SiblingElementMatcher(this, prev.getElementMatcher(), curr.getElementMatcher());
	}

	public int getSpecificity() {
		return addSpecificity(prev.getSpecificity(), curr.getSpecificity());
	}

	public void serialize(PrintWriter out) {
		prev.serialize(out);
		out.print("+");
		curr.serialize(out);
	}

}
