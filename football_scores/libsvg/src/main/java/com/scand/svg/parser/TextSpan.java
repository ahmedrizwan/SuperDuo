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
package com.scand.svg.parser;

/**
	Value object describing hierarchy of text spans from SVG. Each text span
	is inline element describing such textual content to be drawn. Described by
	text properties, parent span (if exists), and just holds the parsed elements.
*/
public class TextSpan {
	public TextSpan parent;
	public String text;
	public TextProperties props;
	public TextSpan() {
	}
	public TextSpan( TextProperties pp ) {
		props = pp;
	}
	public void setText( String str ) {
		str = str.replaceAll("\\s+", " ");
		str = str.replaceAll("(\\s)\\1", " ");
		text = str;
	}
	public String asString() {
		return props.x + ", " + props.y + " " + props.fillColor + ": " + text;
	}
}