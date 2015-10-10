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

import android.graphics.RectF;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class FilterOp {
	public RectF bounds; /* x, y, width, height */
    public Float resX, resY; /* rx, ry: not implemented 'filterRes' */
    public String href;
    public String result;
	public String filterOp;

    public ArrayList<FilterOp> op = new ArrayList<>();

	/* common except fe*Light and feMergeNode */
	/* filterPrimitiveMarginsUnits filterMarginsUnits */
	/* filterUnits */


	/* feFlood */
    public String in;

	/* feFunc* */
    public String funcType = "A"; /* A, R, G, B */
    public String type = "table"; /* identity, table, discrete, linear, gamma */
    public ArrayList<Float> tableValues = new ArrayList<>();
    public Float slope = 1f;
    public Float intercept = 0f;
    public Float amplitude = 1f;
    public Float exponent = 1f;
    public Float offset = 0f;

    /* feMerge */
    /* feBlend */
    public String mode = "normal"; /* '', 'normal', 'multiply', 'screen', 'darken', 'lighten' */
    /* shared public String in; */
    public String in2;

    /* feSpecularLighting */
    /* shared public String in; */
    public Float surfaceScale = 1f;
    public Float specularConstant = 1f;
    public Float specularExponent = 1f;

    /* feTurbulence */
    public String stitchTiles = "no-stitch"; /* '', stitch, no-stitch */
    public String feTurbulence_type = "turbulence"; /* '', fractalNoise, turbulence */
    public Float numOctaves = 1f;
    public Float seed = 0f;

    /* feComposite */
    public String operator="over"; /* '' over in out atop xor arithmetic  */
    /* shared private String in; */
    /* shared private String in2; */
    public Float k1 = 0f;
    public Float k2 = 0f;
    public Float k3 = 0f;
    public Float k4 = 0f;

    /* feColorMatrix */
    public String feColorMatrix_type = "matrix"; /* '' matrix saturate hueRotate luminanceToAlpha */
    /* shared public String in; */
    public ArrayList<Float> values = new ArrayList<>();

	/* feGaussianBlur */
    /* shared public String in; */
    /* std-deviation-[x|y] are not supported */

    /* fePointLight */
    public Float x = 0f;
    public Float y = 0f;
    public Float z = 0f;

    /* feSpotLight */
    /* shared public Float x = 0f; */
    /* shared public Float y = 0f; */
    /* shared public Float z = 0f; */
    public Float pointsAtX = 0f;
    public Float pointsAtY = 0f;
    public Float pointsAtZ = 0f;
    /* shared public Float specularExponent = 1f; */
    public Float limitingConeAngle= 0f;

    /* feConvolveMatrix */
    public String edgeMode = "duplicate"; /* '' duplicate wrap none */
    /* shared public String in; */
    public Float bias = 0f;
    public boolean preserveAlpha = false;
    /* unsupported: order kernelUnitLength kernelMatrix divisor targetX targetY */

    /* feImage */
    public boolean preserveAspectRatio = false;
    public boolean externalResourcesRequired = false;

    /* feComponentTransfer */
    /* shared public String in; */

    /* feOffset */
    /* shared private String in; */
    public Float dx = 0f;
    public Float dy = 0f;

    /* feDistantLight */
    public Float azimuth = 0f;
    public Float elevation = 0f;

    /* feMorphology */
    /* shared private public in; */
    public String feMorphology_operator = "erode"; /* '' erode dilate */
    /* radiusX radiusY are not supported */

    /* feDisplacementMap */
    /* shared public String in; */
    /* shared public String in2; */
    public Float scale = 0f;
    public String xChannelSelector = "A"; /* '' R G B A */
    public String yChannelSelector = "A"; /* '' R G B A */

    /* feMergeNode */
    /* shared public String in; */

    /* feTile */
    /* shared public String in; */

    /* feDiffuseLighting */
    /* shared public String in; */
    /* shared public Float surfaceScale = 1f; */
    public Float diffuseConstant = 1f;

    public FilterOp(String filterOp){
    	this.filterOp = filterOp;
    }

    public void parseFrom(Properties pp) {
        // As we have so much properties we'll fit it via reflections.
        // The defaults are coming from fields.
        for(Field f : this.getClass().getDeclaredFields()) {
            Class ft = f.getType();
            String propName = f.getName();
            if(propName.startsWith(filterOp))
                propName = propName.substring(propName.indexOf('_') + 1);

            try {
                if (ft.equals(Float.class)) {
                    Float v = pp.getFloat(propName, f.getFloat(this));
                    f.setFloat(this, v);
                } else if (ft.equals(String.class)) {
                    String v = pp.getString(propName);
                    if(v == null) v = (String)f.get(this);
                    f.set(this, v);
                } else if (ft.equals(Boolean.class) || ft.equals(boolean.class)) {
                    String v = pp.getString(propName);
                    if(v == null) v = "" + f.getBoolean(this);
                    f.setBoolean(this, v.equals("true"));
                }
            }
            catch(Exception e) {
                continue;
            }
        }
    }
}
