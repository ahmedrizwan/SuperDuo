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

public class AttributeSelector extends Selector {

	String prefix;

	String ns;

	String attr;

	String op;

	CSSValue value;

	AttributeSelector(String prefix, String ns, String attr, String op, CSSValue value) {
		this.prefix = prefix;
		this.ns = ns;
		this.attr = attr;
		this.op = op;
		this.value = value;
	}

	public ElementMatcher getElementMatcher() {
		return new AttributeElementMatcher(this, ns, attr, op, value);
	}

	public int getSpecificity() {
		return 0x100;
	}

	public void serialize(PrintWriter out) {
		out.print("[");
		if (value == null) {
			if (prefix != null) {
				out.print(prefix);
				out.print("|");
			}
			out.print(attr);
		} else {
			if (prefix != null) {
				out.print(prefix);
				out.print("|");
			}
			out.print(attr);
			out.print(op);
			value.serialize(out);
		}
		out.print("]");
	}
	
	public boolean equals(Object obj) {
		if (getClass() != obj.getClass())
			return false;
		AttributeSelector other = (AttributeSelector) obj;
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
		if (value != null) {
			if (other.value == null || !other.value.equals(value))
				return false;
		} else if (other.value != null)
			return false;
		return other.attr.equals(attr) && other.op.equals(op);
	}

	public int hashCode() {
		int code = attr.hashCode() + 11*op.hashCode();
		if (value != null)
			code += value.hashCode();
		if (ns != null)
			code += 3 * ns.hashCode();
		if (prefix != null)
			code += 5 * prefix.hashCode();
		return code;
	}	

}
