package com.scand.svg.css;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

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

/**
 * 
 * Cascade result indexed by media
 */
public class CascadeResult {

	ElementProperties result = new ElementProperties();

	Hashtable byMedia;

	public CascadeResult() {
	}

	protected CascadeResult(CascadeResult other) {
		if (other.result != null)
			result = other.result.cloneObject();
		if (other.byMedia != null) {
			byMedia = new Hashtable();
			Iterator it = other.byMedia.entrySet().iterator();
			while( it.hasNext() ) {
				Map.Entry entry = (Map.Entry)it.next();
				byMedia.put(entry.getKey(), ((ElementProperties)entry.getValue()).cloneObject());
			}
		}
	}

	public Iterator media() {
		if (byMedia == null)
			return null;
		return byMedia.keySet().iterator();
	}

	public ElementProperties getProperties() {
		return result;
	}

	public ElementProperties getPropertiesForMedia(String media) {
		ElementProperties r;
		if (byMedia == null) {
			byMedia = new Hashtable();
			r = null;
		} else
			r = (ElementProperties) byMedia.get(media);
		if (r == null) {
			r = new ElementProperties();
			byMedia.put(media, r);
		}
		return r;
	}

	public void removeMedia(String media) {
		if (byMedia != null) {
			byMedia.remove(media);
			if (byMedia.size() == 0)
				byMedia = null;
		}
	}

	public boolean isEmpty() {
		return (byMedia == null || byMedia.size() == 0) && result.isEmpty();
	}

	public boolean equals(Object obj) {
		if (obj.getClass() != getClass())
			return false;
		CascadeResult other = (CascadeResult) obj;
		if (!result.equals(other.result))
			return false;
		boolean empty = (byMedia == null || byMedia.size() == 0);
		boolean otherEmpty = (other.byMedia == null || other.byMedia.size() == 0);
		if (empty && otherEmpty)
			return true;
		if (empty != otherEmpty)
			return false;
		return byMedia.equals(other.byMedia);
	}

	public int hashCode() {
		int code = result.hashCode();
		if (byMedia != null && byMedia.size() != 0)
			code += byMedia.hashCode();
		return code;
	}

	public CascadeResult cloneObject() {
		return new CascadeResult(this);
	}

}
