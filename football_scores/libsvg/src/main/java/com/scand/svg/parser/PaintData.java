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

import android.graphics.Paint;

import com.scand.svg.parser.support.ColorSVG;

/**
	Implementation state of paint context: colors, strokes, fonts, anchors
*/
class PaintData {
	public ColorSVG fillColor;
	public Float fillOpacity;
	public Gradient gr;
	
	public ColorSVG strokeColor;
	public Paint.Cap strokeCapStyle;
	public Paint.Join strokeJoinStyle;
	public Float strokeWidth;
	public Float strokeOpacity;
	public Numbers dasharray;
    public Float dashOffset;
    public Float opacity;

	public String fontName;
	public Float fontSize;
	public String fontWeight;
	public String fontStyle;
	public String textAnchor;

	public Filter filter;
}