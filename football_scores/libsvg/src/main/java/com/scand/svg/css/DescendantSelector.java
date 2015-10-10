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

public class DescendantSelector extends Selector {

	private Selector ancestor;

	private Selector descendant;

	DescendantSelector(Selector ancestor, Selector descendant) {
		this.descendant = descendant;
		this.ancestor = ancestor;
	}

	public ElementMatcher getElementMatcher() {
		return new DescendantElementMatcher(this, ancestor.getElementMatcher(), descendant.getElementMatcher());
	}

	public int getSpecificity() {
		return addSpecificity(ancestor.getSpecificity(), descendant.getSpecificity());
	}

	public void serialize(PrintWriter out) {
		ancestor.serialize(out);
		out.print(" ");
		descendant.serialize(out);
	}

}
