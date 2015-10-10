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

public class NamedElementSelector extends Selector {

	private String prefix;

	private String ns;

	private String name;

	NamedElementSelector(String prefix, String ns, String name) {
		this.prefix = prefix;
		this.ns = ns;
		this.name = name;
	}

	public ElementMatcher getElementMatcher() {
		return new NamedElementMatcher(this, ns, name);
	}

	public int getSpecificity() {
		return 1;
	}

	public void serialize(PrintWriter out) {
		if (prefix != null) {
			out.print(prefix);
			out.print("|");
		}
		out.print(name);
	}

	public boolean equals(Object obj) {
		if (getClass() != obj.getClass())
			return false;
		NamedElementSelector other = (NamedElementSelector) obj;
		if (prefix != null) {
			if (other.prefix == null || !other.prefix.equals(prefix))
				return false;
		} else if (other.prefix != null)
			return false;
		if (ns != null) {
			if (other.ns == null || !other.ns.equals(ns))
				return false;
		} else if (other.ns != null)
			return false;
		return other.name.equals(name);
	}

	public int hashCode() {
		int code = name.hashCode();
		if (ns != null)
			code += 3 * ns.hashCode();
		if (prefix != null)
			code += 5 * prefix.hashCode();
		return code;
	}

	public boolean hasElementNamespace() {
		return ns != null;
	}

	public String getElementNamespace() {
		return ns;
	}

	public String getElementName() {
		return name;
	}
}
