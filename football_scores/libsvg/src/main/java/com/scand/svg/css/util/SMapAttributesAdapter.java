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
package com.scand.svg.css.util;

import org.xml.sax.Attributes;

public class SMapAttributesAdapter implements SMap {

	private Attributes attributes;

	public SMapAttributesAdapter(Attributes attributes) {
		this.attributes = attributes;
	}

	public Object get(String namespace, String name) {
		return attributes.getValue(namespace, name);
	}

	public SMapIterator iterator() {
		return new SMapIterator() {
			int index;

			public String getName() {
				return attributes.getLocalName(index);
			}

			public String getNamespace() {
				return attributes.getURI(index);
			}

			public Object getValue() {
				return attributes.getValue(index);
			}

			public boolean hasItem() {
				return index < attributes.getLength();
			}

			public void nextItem() {
				index++;
			}
		};
	}

}
