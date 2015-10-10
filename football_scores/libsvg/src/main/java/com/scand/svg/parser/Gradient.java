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


import android.graphics.Matrix;

import com.scand.svg.parser.support.ColorSVG;

import java.util.ArrayList;

/**
	Gradient internal descriptor.
*/
public class Gradient {
    String id;
    String xlink;
    public boolean isLinear;
    public Float x1, y1, x2, y2;
    public Float x, y, radius, fx, fy;
    public String spreadMethod;
    public String gradientUnits;

    public boolean pX1, pX2, pY1, pY2, pX, pY, pR, pFX, pFY;
    
    public ArrayList<Float> positions = new ArrayList<>();
    public ArrayList<ColorSVG> colors = new ArrayList<>();
    public Matrix matrix = null;

    public Gradient createChild(Gradient g) {
        Gradient child = new Gradient();
        child.id = g.id;
        child.xlink = id;
        child.isLinear = g.isLinear;
        child.x1 = g.x1;
        child.x2 = g.x2;
        child.y1 = g.y1;
        child.y2 = g.y2;
        child.x = g.x;
        child.y = g.y;
        child.fx = g.fx;
        child.fy = g.fy;
        child.radius = g.radius;

        child.pX1 = g.pX1;
        child.pX2 = g.pX2;
        child.pY1 = g.pY1;
        child.pY2 = g.pY2;
        child.pX = g.pX;
        child.pY = g.pY;
        child.pR = g.pR;
        child.pFX = g.pFX;
        child.pFY = g.pFY;
        child.spreadMethod=g.spreadMethod;
        child.gradientUnits=g.gradientUnits;
        
        child.positions = positions;
        child.colors = colors;
        child.matrix = matrix;
        if (g.matrix != null) {
            if (matrix == null) {
                child.matrix = g.matrix;
            } else {
                Matrix m = new Matrix(matrix);
                m.postConcat(matrix);
                child.matrix = m;
            }
        }
        return child;
    }
}