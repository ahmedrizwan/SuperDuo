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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class EllipseSVG implements GraphElement {

    public RectF ellipse;
    public RectF bounds;

    public EllipseSVG(float left, float top, float width, float height){
        float x1=left;
        float x2=x1+width;
        float y1=top;
        float y2=y1+height;
        ellipse = new RectF(x1,y1,x2,y2);
        bounds = new RectF(x1,y1,width,height);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawOval(ellipse, paint);
    }
}
