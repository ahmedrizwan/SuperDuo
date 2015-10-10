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

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Shader;

public class LinearGradientPaint {

    public float x1,y1,x2,y2;
    public float[] positions;
    public ColorSVG[] colors;
    Shader.TileMode mTileMode;
    Matrix matrix;

    public LinearGradientPaint(float x1,float y1,float x2,float y2, float[] positions, ColorSVG[] colors, Shader.TileMode tileMode){
        this.x1=x1;
        this.y1=y1;
        this.x2=x2;
        this.y2=y2;
        this.positions=positions;
        this.colors=colors;
        this.mTileMode=tileMode;
    }

    public LinearGradientPaint(PointF start, PointF end, float[] positions, ColorSVG[] colors, Shader.TileMode tileMode, Matrix matrix){
        this(start.x, start.y,end.x,end.y,positions,colors,tileMode);
        this.matrix=matrix;
    }

}
