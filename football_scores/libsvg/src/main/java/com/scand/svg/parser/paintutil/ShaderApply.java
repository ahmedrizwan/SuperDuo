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
package com.scand.svg.parser.paintutil;

import android.graphics.PointF;
import android.graphics.Shader;

import com.scand.svg.parser.Gradient;
import com.scand.svg.parser.support.ColorSVG;
import com.scand.svg.parser.support.GraphicsSVG;
import com.scand.svg.parser.support.LinearGradientPaint;
import com.scand.svg.parser.support.RadialGradientPaint;

public class ShaderApply {
    //Works correctly only for gradientUnits='objectBoundingBox' or for gradientUnits='userSpaceOnUse' without percents
   	public static void applyShader(Gradient gr, float xx, float yy, float width, float height, GraphicsSVG canvas, ColorSVG alphaColor) {

		if (gr.colors.size() < 2 || gr.positions.size() < 2) {
			return;
		}

		ColorSVG[] colors = new ColorSVG[gr.colors.size()];
		for (int i = 0; i < colors.length; i++) {
			colors[i] = gr.colors.get(i);
		}

		float[] positions = new float[gr.positions.size()];
		for (int i = 0; i < positions.length; i++) {
			Object o = gr.positions.get(i);
			if (o != null) {
				positions[i] = ((Float) o);
			}
		}

        Shader.TileMode tileMode= Shader.TileMode.CLAMP;
        if (gr.spreadMethod!=null) {
            if (gr.spreadMethod.equals("reflect")) {
                tileMode = Shader.TileMode.MIRROR;
            } else if (gr.spreadMethod.equals("repeat")) {
                tileMode = Shader.TileMode.REPEAT;
            }
        }

        boolean isObjectBoundingBox=gr.gradientUnits.equals("objectBoundingBox");

		if (gr.isLinear) {
			float x1 = gr.pX1||isObjectBoundingBox? gr.x1 * width + xx : gr.x1;
			float y1 = gr.pY1||isObjectBoundingBox? gr.y1 * height + yy : gr.y1;
			float x2 = gr.pX2||isObjectBoundingBox? gr.x2 * width + xx : gr.x2;
			float y2 = gr.pY2||isObjectBoundingBox? gr.y2 * height + yy : gr.y2;

			LinearGradientPaint g;
			if (gr.matrix != null) {
				PointF start = new PointF(x1, y1);
				PointF end = new PointF(x2, y2);
				g = new LinearGradientPaint(start, end, positions, colors,tileMode, gr.matrix);
			} else {
				g = new LinearGradientPaint(x1, y1, x2, y2, positions, colors,  tileMode);
			}

            canvas.setGradient(g,alphaColor);
		} else {
            /*same reason like in linear gradient*/
			float x = gr.pX||isObjectBoundingBox ? gr.x * width + xx : gr.x;
			float y = gr.pY||isObjectBoundingBox ? gr.y * height + yy: gr.y;
			float fx =gr.pFX||isObjectBoundingBox ? gr.fx * width + xx: gr.fx;
			float fy = gr.pFY||isObjectBoundingBox ? gr.fy * height + yy: gr.fy;
			float rad = gr.pR||isObjectBoundingBox ? gr.radius * width : gr.radius;

			if (rad <= 0) {
				return;
			}

			RadialGradientPaint g;
			if (gr.matrix != null) {
				PointF center = new PointF(x, y);
				PointF focus = new PointF(fx, fy);
				g = new RadialGradientPaint(center, rad, focus, positions,
						colors, tileMode, gr.matrix);
			} else {
				g = new RadialGradientPaint(x, y, rad, fx, fy , positions, colors, tileMode);
			}

			canvas.setGradient(g,alphaColor);
		}
	}
}
