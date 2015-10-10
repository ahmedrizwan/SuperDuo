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

public class CSSLength extends CSSValue {

	double value;

	String unit;

	public CSSLength(double value, String unit) {
		this.value = value;
		this.unit = unit;
	}

	public double getValue() {
		return value;
	}

	public String getUnit() {
		return unit;
	}

	public String toString() {
		double sv = Math.round(value * 1000) / 1000.0;
		if (sv == (int) sv)
			return (int) sv + unit;
		return sv + unit;
	}

	public void serialize(PrintWriter out) {
		double sv = Math.round(value * 1000) / 1000.0;
		if (sv == (int) sv)
			out.print((int) sv);
		else
			out.print(sv);
		out.print(unit);
	}

	public int hashCode() {
		return (int) Math.round(value * 1000) + unit.hashCode();
	}

	public boolean equals(Object other) {
		if (other.getClass() == getClass()) {
			CSSLength o = (CSSLength) other;
			return o.value == value && o.unit.equals(unit);
		}
		return false;
	}
}
