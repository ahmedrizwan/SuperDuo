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

import com.scand.svg.css.util.*;

public abstract class ElementMatcher {

	private Selector selector;

	ElementMatcher(Selector selector) {
		this.selector = selector;
	}

	public Selector getSelector() {
		return selector;
	}

	/**
	 * Matches an element with a given namespace, name and attributes
	 * 
	 * @param ns
	 *            element's namespace
	 * @param name
	 *            element's local name
	 * @param attrs
	 *            element's attributes
	 * @return MatchResult if element matches this selector, null otherwise
	 */
	public abstract MatchResult pushElement(String ns, String name, SMap attrs);

	/**
	 * Finish element's processing. Note that pushElement/popElement calls
	 * should correspond to the elements nesting.
	 */
	public abstract void popElement();

}
