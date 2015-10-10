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
package com.scand.svg.parser.support;

import android.graphics.Paint;

public class BasicStroke {

    public float mLineWidth;
    public Paint.Cap mCapStyle;
    public Paint.Join mJoinStyle;
    public float[] mDashArray;
    public float mDashOffset;

    public BasicStroke( float lineWidth, Paint.Cap capStyle,Paint.Join joinStyle){
        mLineWidth = lineWidth;
        mCapStyle = capStyle;
        mJoinStyle = joinStyle;

    }

    public BasicStroke( float lineWidth, Paint.Cap capStyle,Paint.Join joinStyle, float f1, float[] dash, float dashOffset){
        mLineWidth = lineWidth;
        mCapStyle = capStyle;
        mJoinStyle = joinStyle;
        mDashArray = dash;
        mDashOffset = dashOffset;
        //TODO rest data
    }

}
