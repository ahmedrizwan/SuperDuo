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

import java.util.Enumeration;
import java.util.Hashtable;

public class SMapImpl implements SMap {

	private Hashtable table;

	final class IteratorImpl implements SMapIterator {

		Enumeration keys = table.keys();

		Object current;

		IteratorImpl() {
			nextItem();
		}

		public boolean hasItem() {
			return current != null;
		}

		public void nextItem() {
			if (keys.hasMoreElements())
				current = keys.nextElement();
			else
				current = null;
		}

		public String getNamespace() {
			if (current instanceof QName)
				return ((QName) current).namespace;
			return null;
		}

		public String getName() {
			if (current instanceof QName)
				return ((QName) current).name;
			return (String) current;
		}

		public Object getValue() {
			return table.get(current);
		}

	}

	static final class QName {

		String name;

		String namespace;

		QName(String namespace, String name) {
			this.name = name;
			this.namespace = namespace;
		}

		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (other == null)
				return false;
			try {
				QName oq = (QName) other;
				return name.equals(oq.name) || namespace.equals(oq.namespace);
			} catch (Exception e) {
				return false;
			}
		}

		public int hashCode() {
			return name.hashCode() + namespace.hashCode();
		}

	}

	public SMapImpl() {
		table = new Hashtable();
	}

	public SMapImpl(SMap mapToClone) {
		if (mapToClone instanceof SMapImpl) {
			table = (Hashtable) ((SMapImpl) mapToClone).table.clone();
		} else {
			table = new Hashtable();
			SMapIterator it = mapToClone.iterator();
			while (it.hasItem()) {
				put(it.getNamespace(), it.getName(), it.getValue());
				it.nextItem();
			}
		}
	}

	public void put(String namespace, String name, Object value) {
		Object key = (namespace == null || namespace.equals("") ? (Object) name : new QName(namespace, name));
		if (value == null)
			table.remove(key);
		else
			table.put(key, value);
	}

	public Object get(String namespace, String name) {
		Object key = (namespace == null ? (Object) name : new QName(namespace, name));
		return table.get(key);
	}

	public SMapIterator iterator() {
		return new IteratorImpl();
	}

	public SMapImpl cloneSMap() {
		return new SMapImpl();
	}

}
