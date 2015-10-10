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

public class ClassElementMatcher extends ElementMatcher {

	private String className;

	private static final String fb2NS = "http://www.gribuser.ru/xml/fictionbook/2.0";

	ClassElementMatcher(ClassSelector selector, String className) {
		super(selector);
		this.className = className;
	}

	public void popElement() {
	}

	public static String getClassAttribute(String ns, String name) {
		if (ns.equals(fb2NS)) {
			// By FB2's designer's infinite wisdom, CSS class is allowed only
			// on an element named "style" and is given by the attribute named
			// "name". Go figure.
			if (name.equals("style"))
				return "name";
			return null;
		}
		// assume it is "class" (holds true for XHTML and SVG, but questionable
		// for other XML dialects)
		return "class";
	}

	public MatchResult pushElement(String ns, String name, SMap attrs) {
		if (attrs == null)
			return null;
		String classAttrName = getClassAttribute(ns, name);
		if (classAttrName == null)
			return null;
		Object classValue = attrs.get("", classAttrName);
		if (classValue == null)
			return null;
		return AttributeElementMatcher.isInList(classValue, className) ? MatchResult.ALWAYS : null;
	}

}
