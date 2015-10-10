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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
	Text from SVG is a collection of text spans.
*/
public class Text extends TextSpan {
	public ArrayList spans = new ArrayList();
	private Stack currentSpans = new Stack();
	public Text() {
		super();
	}
	public Text(TextProperties pp) {
		super(pp);
		spans.add(this);
	}
	public void addSpan( TextProperties pp ) {
		TextSpan currentSpan = new TextSpan( pp );
		spans.add(currentSpan);
		currentSpans.push(currentSpan);
		currentSpan.parent = this;
	}
	public void endSpan() {
		currentSpans.pop();
	}
	public void setText( String str ) {
		if ( str.trim().length() == 0 ) {
			return;
		}
		if ( currentSpans.size() > 0 && ((TextSpan)currentSpans.peek()).text == null ) {
			((TextSpan)currentSpans.peek()).setText(str);
		} else {
			if ( spans.size() > 1 ) {
				addSpan( props.cloneStyle() );
				setText(str);
			} else {
				super.setText(str);
			}
		}
	}
	public String toString() {
		String result = "";
		Iterator ii = spans.iterator();
		while (ii.hasNext()) {
			TextSpan tt = (TextSpan)ii.next();
			result += tt.asString() + "\n";
		}
		return "[" + result.substring(0, result.length()-1) + "]";
	}
}