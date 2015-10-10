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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class CSSStylesheet {
	Vector statements = new Vector();

	Hashtable rulesBySelector = new Hashtable();

	public void add(Object rule) {
		if (rule instanceof SelectorRule) {
			SelectorRule srule = (SelectorRule) rule;
			Selector[] selectors = srule.selectors;
			if (selectors.length == 1)
				rulesBySelector.put(selectors[0], rule);
		}
		statements.add(rule);
	}

	public Selector getSimpleSelector(String elementName, String className) {
		NamedElementSelector elementSelector = null;
		if (elementName != null)
			elementSelector = new NamedElementSelector(null, null, elementName);
		if (className == null)
			return elementSelector;
		Selector selector = new ClassSelector(className);
		if (elementSelector != null)
			selector = new AndSelector(elementSelector, selector);
		return selector;
	}

	public SelectorRule getRuleForSelector(Selector selector, boolean create) {
		SelectorRule rule = (SelectorRule) rulesBySelector.get(selector);
		if (rule == null && create) {
			Selector[] selectors = { selector };
			rule = new SelectorRule(selectors);
			add(rule);
		}
		return rule;
	}

	public void serialize(PrintWriter out) {
		Iterator list = statements.iterator();
		while (list.hasNext()) {
			Object stmt = list.next();
			if (stmt instanceof FontFaceRule) {
				((FontFaceRule) stmt).serialize(out);
				out.println();
			} else if (stmt instanceof BaseRule) {
				((SelectorRule) stmt).serialize(out);
				out.println();
			} else if (stmt instanceof MediaRule) {
				((MediaRule) stmt).serialize(out);
				out.println();
			} else if (stmt instanceof ImportRule) {
				((ImportRule) stmt).serialize(out);
				out.println();
			} else if (stmt instanceof PageRule) {
				((PageRule) stmt).serialize(out);
				out.println();
			}
		}
	}

	public Iterator statements() {
		return statements.iterator();
	}
}
