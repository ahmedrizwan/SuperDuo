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

public class ColorSVG {
    public int mValue;

    public boolean isNone=false;

    public static ColorSVG createNoneColor(){
        ColorSVG colorSVG = new ColorSVG(0);
        colorSVG.isNone=true;
        return colorSVG;
    }

    public ColorSVG(int r, int g, int b){
        mValue = android.graphics.Color.argb(255,r,g,b);
    }

    public ColorSVG(int value){
        mValue=value;
    }

    public ColorSVG(int value, boolean withAlpha){
        if (withAlpha){
            mValue = android.graphics.Color.argb(android.graphics.Color.alpha(value),android.graphics.Color.red(value),android.graphics.Color.green(value),android.graphics.Color.blue(value));
        }else{
            mValue = android.graphics.Color.argb(255, android.graphics.Color.red(value), android.graphics.Color.green(value), android.graphics.Color.blue(value));
        }
    }

    public int getRGB(){
        return mValue;
    }

    public int getAlpha(){
        return android.graphics.Color.alpha(mValue);
    }

}
