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

public class RectangleSVG extends RectF implements GraphElement {

    public float rx,ry;

    public RectangleSVG(float x1, float y1, float x2, float y2, float rx, float ry){
         super(x1,y1,x1+x2,y1+y2);
         this.rx=rx;
         this.ry=ry;
    }

    public RectangleSVG(float x1, float y1, float x2, float y2){
        super(x1,y1,x1+x2,y1+y2);
        rx=0;
        ry=0;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (rx!=0 && ry!=0) {
            canvas.drawRoundRect(this, rx, ry, paint);
        } else {
            canvas.drawRect(this, paint);
        }
    }
}
