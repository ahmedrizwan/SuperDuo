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

public class NamedElementMatcher extends ElementMatcher {

	private String ns;

	private String name;

	public NamedElementMatcher(NamedElementSelector selector, String ns, String name) {
		super(selector);
		this.ns = ns;
		this.name = name;
	}

	public void popElement() {
	}

	public MatchResult pushElement(String ns, String name, SMap attrs) {
		return this.name.equals(name) && (this.ns == null || this.ns.equals(ns == null ? "" : ns)) ? MatchResult.ALWAYS
				: null;
	}

}
