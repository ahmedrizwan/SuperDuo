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
package com.scand.svg;

import android.graphics.Bitmap;
import android.graphics.RectF;

/**
	Just holder for final result of the parsing / rendering.
*/
public class SVG {

    private Bitmap image;
    private RectF bounds;
    private RectF limits;

    public SVG(Bitmap picture, RectF bounds) {
        this.image = picture;
        this.bounds = bounds;
    }

    public void setLimits(RectF limits) {
        this.limits = limits;
    }

    public Bitmap getBitmap() {
        return image;
    }

    public RectF getBounds() {
        return bounds;
    }

    public RectF getLimits() {
        return limits;
    }
}
