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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class ElementProperties implements Cloneable {

	private InlineRule result = new InlineRule();

	private Hashtable byPseudoElement;

	ElementProperties() {
	}

	protected ElementProperties(ElementProperties other) {
		if (other.result != null)
			result = other.result.cloneObject();
		if (other.byPseudoElement != null) {
			byPseudoElement = new Hashtable();
			Iterator it = other.byPseudoElement.entrySet().iterator();
			while( it.hasNext() ) {
				Map.Entry entry = (Map.Entry)it.next();
				byPseudoElement.put(entry.getKey(), ((InlineRule)entry.getValue()).cloneObject());
			}
		}
	}

	public Iterator pseudoElements() {
		if (byPseudoElement == null)
			return null;
		return byPseudoElement.keySet().iterator();
	}

	public InlineRule getPropertySet() {
		return result;
	}

	public InlineRule getPropertySetForPseudoElement(String pseudoElement) {
		InlineRule r;
		if (byPseudoElement == null) {
			byPseudoElement = new Hashtable();
			r = null;
		} else
			r = (InlineRule) byPseudoElement.get(pseudoElement);
		if (r == null) {
			r = new InlineRule();
			byPseudoElement.put(pseudoElement, r);
		}
		return r;
	}

	public void removePseudoElement(String pseudoElement) {
		if (byPseudoElement != null) {
			byPseudoElement.remove(pseudoElement);
			if (byPseudoElement.size() == 0)
				byPseudoElement = null;
		}
	}

	public boolean isEmpty() {
		return (byPseudoElement == null || byPseudoElement.size() == 0) && result.isEmpty();
	}

	public boolean equals(Object obj) {
		if (obj.getClass() != getClass())
			return false;
		ElementProperties other = (ElementProperties) obj;
		if (!result.equals(other.result))
			return false;
		boolean empty = (byPseudoElement == null || byPseudoElement.size() == 0);
		boolean otherEmpty = (other.byPseudoElement == null || other.byPseudoElement.size() == 0);
		if (empty && otherEmpty)
			return true;
		if (empty != otherEmpty)
			return false;
		return byPseudoElement.equals(other.byPseudoElement);
	}

	public int hashCode() {
		int code = result.hashCode();
		if (byPseudoElement != null && byPseudoElement.size() != 0)
			code += byPseudoElement.hashCode();
		return code;
	}

	public ElementProperties cloneObject() {
		return new ElementProperties(this);
	}

}
