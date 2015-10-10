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

import com.scand.svg.parser.support.ColorSVG;

/**
	Text properties: position, color, stroke, font, anchor.
*/
public class TextProperties {
	public Float x;
	public Float y;
	public Float dx = new Float(0f);
	public Float dy = new Float(0f);
	public ColorSVG fillColor;
	public ColorSVG strokeColor;
	public Float strokeWidth;

	String fontName;
	Float fontSize;
	String fontWeight;
	String fontStyle;
	String textAnchor;
	String extUrl;

	public TextProperties() {
	}

	public TextProperties( Properties props, PaintData ctx ) {
		x = props.getFloat("x");
		y = props.getFloat("y");
		dx = props.getFloat("dx", new Float(0f));
		dy = props.getFloat("dy", new Float(0f));
		fillColor = props.getColor("fill", ctx);
		strokeColor = props.getColor("stroke", ctx);
		strokeWidth = props.getFloat("stroke-width");
		if (strokeWidth == null && strokeColor != null) {
			strokeWidth = new Float(1f);
		}
		if (strokeColor == null && strokeWidth != null && strokeWidth > 0 ) {
			if ( fillColor != null ) {
				strokeColor = fillColor;
			} else {
				strokeColor.mValue = android.graphics.Color.BLACK;
			}
		}

		fontName = props.getString("font-family");
		fontSize = props.getFloat("font-size", (ctx != null ? ctx.fontSize : null));
		fontWeight = props.getString("font-weight");
		fontStyle = props.getString("font-style");
		textAnchor = props.getString("text-anchor");
		extUrl = props.getString("src");
	}
	
	public TextProperties cloneStyle() {
		TextProperties t = new TextProperties();
		t.fillColor = fillColor;
		t.strokeColor = strokeColor;
		t.strokeWidth = strokeWidth;
		
		t.fontName = fontName;
		t.fontSize = fontSize;
		t.fontWeight = fontWeight;
		t.fontStyle = fontStyle;
		t.textAnchor = textAnchor;
		return t;
	}
}