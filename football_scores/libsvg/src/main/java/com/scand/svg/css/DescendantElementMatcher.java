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

import com.scand.svg.css.util.SMap;

public class DescendantElementMatcher extends ElementMatcher {

	private ElementMatcher ancestor;

	private ElementMatcher descendant;

	private int ancestorMatchedDepth;

	DescendantElementMatcher(DescendantSelector selector, ElementMatcher parent, ElementMatcher child) {
		super(selector);
		this.ancestor = parent;
		this.descendant = child;
	}

	public void popElement() {
		if (ancestorMatchedDepth > 0)
			descendant.popElement();
		else
			ancestor.popElement();
		ancestorMatchedDepth--;
	}

	public MatchResult pushElement(String ns, String name, SMap attrs) {
		if (ancestorMatchedDepth > 0) {
			ancestorMatchedDepth++;
			return descendant.pushElement(ns, name, attrs);
		} else {
			MatchResult r = ancestor.pushElement(ns, name, attrs);
			if( r != null && r.getPseudoElement() == null )
				ancestorMatchedDepth = 1;
			return null;
		}
	}

}
