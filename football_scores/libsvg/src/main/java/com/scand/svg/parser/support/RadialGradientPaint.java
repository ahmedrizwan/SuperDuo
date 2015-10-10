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

public class RadialGradientPaint {

    public float x,y,rad,fx,fy;
    public float[] positions;
    public ColorSVG[] colors;
    public Matrix matrix;
    public Shader.TileMode tileMode;

    public RadialGradientPaint(PointF center, float rad, PointF focus, float[] positions, ColorSVG[] colors, Shader.TileMode tileMode,Matrix matrix){
        this(center.x,center.y,rad,focus.x,focus.y,positions,colors,tileMode);
        this.matrix=matrix;
    }

    public RadialGradientPaint (float x, float y,float rad, float fx, float fy, float[] positions, ColorSVG[] colors, Shader.TileMode tileMode){
        //adroid don't has focus radial gradients((
        this.x=x;
        this.y=y;
        this.rad=rad;
        this.tileMode=tileMode;
        this.positions=positions;
        this.colors =colors;
    }

}
