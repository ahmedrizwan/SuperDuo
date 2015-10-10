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

import android.graphics.Path;

public class ClipPath {

    public static String id;

    private Path mPath;

    public ClipPath(){
        mPath=new Path();
    }

    public void addElement(RectangleSVG element){
        mPath.addRect(element, Path.Direction.CW);
    }

    public void addElement(EllipseSVG element){
        mPath.addOval(element.ellipse, Path.Direction.CW);
    }

    public void addElement(PathExt element){
        mPath.addPath(element);
    }

    public void addElement(LineSVG element){
        Path path = new Path();
        path.moveTo(element.x1, element.y1);
        path.lineTo(element.x2, element.y2);
        mPath.addPath(path);
    }

    public Path getPath(){
        return mPath;
    }

}
