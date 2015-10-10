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

public class CSSRGBColor extends CSSValue {

	int rgb;

	public CSSRGBColor(int rgb) {
		if( (rgb & 0xFF000000) != 0 )
			throw new RuntimeException("invalid parameter");
		this.rgb = rgb;
	}

	public String toString() {
		return "#" + Integer.toHexString(rgb + 0x1000000).substring(1);
	}

	public void serialize(PrintWriter out) {
		out.print("#");
		out.print(Integer.toHexString(rgb + 0x1000000).substring(1));
	}

	public int hashCode() {
		return rgb;
	}

	public boolean equals(Object other) {
		if (other.getClass() == getClass()) {
			CSSRGBColor o = (CSSRGBColor) other;
			return o.rgb == rgb;
		}
		return false;
	}	
}
