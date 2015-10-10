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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class ImageRefSVG implements GraphElement {
    public RectF bounds;
    public boolean keepAR = true;
    public boolean fake = false;
    public Bitmap bitmap = null;

    public ImageRefSVG(float left, float top, float width, float height){
        bounds = new RectF(left, top, width + left, height + top);
        fake = true;
    }

    public ImageRefSVG(float left, float top, float width, float height, boolean _fake, boolean _keepAR){
        bounds = new RectF(left, top, width + left, height + top);
        fake = _fake;
        keepAR = _keepAR;
    }

    public ImageRefSVG(float left, float top, float width, float height, boolean _keepAR, Bitmap _bitmap){
        bounds = new RectF(left, top, width + left, height + top);
        fake = false;
        keepAR = _keepAR;
        bitmap = _bitmap;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if(fake) return;
        canvas.drawBitmap(bitmap, bounds.left, bounds.top, paint);
    }
}
