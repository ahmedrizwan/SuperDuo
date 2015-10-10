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
import android.graphics.Path;

import com.scand.svg.parser.support.PathExt;

/**
	Parser for &lt;path />
*/
public class PathSvg {
	
    /** Constant for having only control point C0 in effect. C0 is the point
     * through which the curve passes. */
    public final static int C0_MASK = 0;
    /** Constant for having control point C1 in effect (in addition
     * to C0). C1 controls the curve going towards C0.
     * */
    public final static int C1_MASK = 1;
    /** Constant for having control point C2 in effect (in addition to C0).
     * C2 controls the curve going away from C0.
     */
    public final static int C2_MASK = 2;
    /** Constant for having control points C1 and C2 in effect (in addition to C0). */
    public final static int C1C2_MASK = C1_MASK | C2_MASK;
	
	
    /**
     * Uppercase rules are absolute positions, lowercase are relative.
     *
     * <ol>
     * <li>M/m - (x y)+ - Move to (without drawing)
     * <li>Z/z - (no params) - Close path (back to starting point)
     * <li>L/l - (x y)+ - Line to
     * <li>H/h - x+ - Horizontal ine to
     * <li>V/v - y+ - Vertical line to
     * <li>C/c - (x1 y1 x2 y2 x y)+ - Cubic bezier to
     * <li>S/s - (x2 y2 x y)+ - Smooth cubic bezier to (shorthand that assumes the x2, y2 from previous C/S is the x1, y1 of this bezier)
     * <li>Q/q - (x1 y1 x y)+ - Quadratic bezier to
     * <li>T/t - (x y)+ - Smooth quadratic bezier to (assumes previous control point is "reflection" of last one w.r.t. to current point)
     * </ol>
     *
     * @param path the path string from the XML
     */
	public static PathExt parsePath(String path) {
        PathExt p = new PathExt();
        p.setFillType(Path.FillType.EVEN_ODD);
        if ( path == null || (path!=null && path.length()==0)) {
        	return p;
        }
        int n = path.length();
        FloatSequence ph = new FloatSequence(path, 0);
        ph.skipWhitespace();
        float lastX = 0;
        float lastY = 0;
        float lastX1 = 0;
        float lastY1 = 0;
        
        boolean lastControlPointDefined = false;
        float lastControlX = 0;
        float lastControlY = 0;
        
        float subPathStartX = 0;
        float subPathStartY = 0;
        char prevCmd = 0;
        while (ph.pos < n) {
            char cmd = path.charAt(ph.pos);
            switch (cmd) {
                case '-':
                case '+':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (prevCmd == 'm' || prevCmd == 'M') {
                        cmd = (char) (((int) prevCmd) - 1);
                        break;
                    } else if (prevCmd != 'z' || prevCmd != 'Z') {
                        cmd = prevCmd;
                        break;
                    }
                default: {
                    ph.advance();
                    prevCmd = cmd;
                }
            }

            boolean wasCurve = false;
            switch (cmd) {
                case 'M':
                case 'm': {
                    lastControlPointDefined = false;
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'm') {
                        subPathStartX += x;
                        subPathStartY += y;
                        lastX += x;
                        lastY += y;
                        p.moveTo(lastX, lastY);
                    } else {
                        subPathStartX = x;
                        subPathStartY = y;
                        p.moveTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    break;
                }
                case 'Z':
                case 'z': {
                    lastControlPointDefined = false;
                    p.close();
                    p.moveTo(subPathStartX, subPathStartY);
                    lastX = subPathStartX;
                    lastY = subPathStartY;
                    lastX1 = subPathStartX;
                    lastY1 = subPathStartY;
                    wasCurve = true;
                    break;
                }
                case 'L':
                case 'l': {
                    lastControlPointDefined = false;
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'l') {
                        lastX += x;
                        lastY += y;
                        p.lineTo(lastX, lastY);
                    } else {
                        p.lineTo(x, y);
                        lastX = x;
                        lastY = y;
                    }
                    break;
                }
                case 'H':
                case 'h': {
                    lastControlPointDefined = false;
                    float x = ph.nextFloat();
                    if (cmd == 'h') {
                        lastX += x;
                        p.lineTo(lastX, lastY);
                    } else {
                        p.lineTo(x, lastY);
                        lastX = x;
                    }
                    break;
                }
                case 'V':
                case 'v': {
                    lastControlPointDefined = false;
                    float y = ph.nextFloat();
                    if (cmd == 'v') {
                        lastY += y;
                        p.lineTo(lastX, lastY);
                    } else {
                        p.lineTo(lastX, y);
                        lastY = y;
                    }
                    break;
                }
                case 'C':
                case 'c': {
                    lastControlPointDefined = false;
                    wasCurve = true;
                    float x1 = ph.nextFloat();
                    float y1 = ph.nextFloat();
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'c') {
                        x1 += lastX;
                        x2 += lastX;
                        x += lastX;
                        y1 += lastY;
                        y2 += lastY;
                        y += lastY;
                    }
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'Q':
                case 'q': {
                    wasCurve = true;
                    float x1 = ph.nextFloat();
                    float y1 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'q') {
                        x1 += lastX;
                        x += lastX;
                        y1 += lastY;
                        y += lastY;
                    }
                    p.quadTo(x1, y1, x, y);
                    lastControlX = x + (x - x1);
                    lastControlY = y + (y - y1);
                    lastControlPointDefined = true;
                    lastX1 = x;
                    lastY1 = y;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'T':
                case 't': {
                    wasCurve = true;
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 't') {
                        x += lastX;
                        y += lastY;
                    }
                    
                    if ( lastControlPointDefined ) {
                        p.quadTo(lastControlX, lastControlY, x, y);
                        lastControlX = x + (x - lastControlX);
                        lastControlY = y + (y - lastControlY);
                    } else {
                        p.quadTo(lastX, lastY, x, y);
                        lastControlX = x + (x - lastX);
                        lastControlY = y + (y - lastY);
                        lastControlPointDefined = true;
                    }
                    lastX1 = x;
                    lastY1 = y;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'S':
                case 's': {
                    lastControlPointDefined = false;
                    wasCurve = true;
                    float x2 = ph.nextFloat();
                    float y2 = ph.nextFloat();
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 's') {
                        x2 += lastX;
                        x += lastX;
                        y2 += lastY;
                        y += lastY;
                    }
                    float x1 = 2 * lastX - lastX1;
                    float y1 = 2 * lastY - lastY1;
                    p.cubicTo(x1, y1, x2, y2, x, y);
                    lastX1 = x2;
                    lastY1 = y2;
                    lastX = x;
                    lastY = y;
                    break;
                }
                case 'A':
                case 'a': {
                    lastControlPointDefined = false;
                    float rx = ph.nextFloat();
                    float ry = ph.nextFloat();
                    float theta = ph.nextFloat();
                    boolean largeArc = ph.nextFloat() != 0;
                    boolean sweepArc = ph.nextFloat() != 0;
                    float x = ph.nextFloat();
                    float y = ph.nextFloat();
                    if (cmd == 'a') {
                        x += lastX;
                        y += lastY;
                    }

//            		double x0 = lastX; // ((lastPoint.mask & C2_MASK) == C2_MASK) ? lastPoint.x[2] : lastPoint.x[0]; // XXX
//            		double y0 = lastY; // ((lastPoint.mask & C2_MASK) == C2_MASK) ? lastPoint.y[2] : lastPoint.y[0];
                    
                    arcTo(p, lastX, lastY, rx, ry, theta, largeArc, sweepArc, x, y);
                    
                    lastX = x;
                    lastY = y;
                    break;
                }
            }
            if (!wasCurve) {
                lastX1 = lastX;
                lastY1 = lastY;
            }
            ph.skipWhitespace();
        }
        return p;
    }


    private static void arcTo(Path path, float x0, float y0, float rx, float ry, float ang, boolean xAxisRotation, boolean isSweep, float x, float y)
    {
        if (x0 == x && y0 == y) {
            return;
        }

        if (rx == 0 || ry == 0) {
            path.lineTo(x, y);
            return;
        }

        rx = Math.abs(rx);
        ry = Math.abs(ry);

        float  angleRadians = (float) Math.toRadians(ang % 360.0);
        double cosValue= Math.cos(angleRadians);
        double sinValue = Math.sin(angleRadians);

        double dx2 = (x0 - x) / 2.0;
        double dy2 = (y0 - y) / 2.0;

        double x1 = (cosValue * dx2 + sinValue * dy2);
        double y1 = (-sinValue * dx2 + cosValue * dy2);

        double rxrx = rx * rx;
        double ryry = ry * ry;
        double x1x1 = x1 * x1;
        double y1y1 = y1 * y1;

        double checkRad = x1x1 / rxrx + y1y1 / ryry;
        if (checkRad > 1) {
            rx = (float) Math.sqrt(checkRad) * rx;
            ry = (float) Math.sqrt(checkRad) * ry;
            rxrx = rx * rx;
            ryry = ry * ry;
        }

        double sign = (xAxisRotation == isSweep) ? -1 : 1;
        double sq = ((rxrx * ryry) - (rxrx * y1y1) - (ryry * x1x1)) / ((rxrx * y1y1) + (ryry * x1x1));
        sq = (sq < 0) ? 0 : sq;
        double coef = (sign * Math.sqrt(sq));
        double cx1 = coef * ((rx * y1) / ry);
        double cy1 = coef * -((ry * x1) / rx);

        double sx2 = (x0 + x) / 2.0;
        double sy2 = (y0 + y) / 2.0;
        double cx = sx2 + (cosValue * cx1 - sinValue * cy1);
        double cy = sy2 + (sinValue * cx1 + cosValue * cy1);

        double ux = (x1 - cx1) / rx;
        double uy = (y1 - cy1) / ry;
        double vx = (-x1 - cx1) / rx;
        double vy = (-y1 - cy1) / ry;
        double p, n;

        n = Math.sqrt((ux * ux) + (uy * uy));
        p = ux;
        sign = (uy < 0) ? -1.0 : 1.0;
        double angleBegin = Math.toDegrees(sign * Math.acos(p / n));

        n = Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0 : 1.0;
        double angleExt = Math.toDegrees(sign * Math.acos(p / n));
        if (!isSweep && angleExt > 0) {
            angleExt -= 360f;
        } else if (isSweep && angleExt < 0) {
            angleExt += 360f;
        }
        angleExt %= 360f;
        angleBegin %= 360f;

        int    segments = (int) Math.ceil(Math.abs(angleExt) / 90.0);

        angleBegin = Math.toRadians(angleBegin);
        angleExt = Math.toRadians(angleExt);
        float  angleIncr = (float) (angleExt / segments);

        double  controlLen = 4.0 / 3.0 * Math.sin(angleIncr / 2.0) / (1.0 + Math.cos(angleIncr / 2.0));

        float[] cubicPoints = new float[segments * 6];
        int     pos = 0;

        for (int i=0; i<segments; i++)
        {
            double  angle = angleBegin + i * angleIncr;

            double  dx = Math.cos(angle);
            double  dy = Math.sin(angle);

            cubicPoints[pos++]   = (float) (dx - controlLen * dy);
            cubicPoints[pos++] = (float) (dy + controlLen * dx);

            angle += angleIncr;
            dx = Math.cos(angle);
            dy = Math.sin(angle);
            cubicPoints[pos++] = (float) (dx + controlLen * dy);
            cubicPoints[pos++] = (float) (dy - controlLen * dx);

            cubicPoints[pos++] = (float) dx;
            cubicPoints[pos++] = (float) dy;
        }

        Matrix arcMatrix = new Matrix();
        arcMatrix.postScale(rx, ry);
        arcMatrix.postRotate(ang);
        arcMatrix.postTranslate((float) cx, (float) cy);
        arcMatrix.mapPoints(cubicPoints);

        cubicPoints[cubicPoints.length-2] = x;
        cubicPoints[cubicPoints.length-1] = y;

        for (int i=0; i<cubicPoints.length; i+=6)
        {
            path.cubicTo(cubicPoints[i], cubicPoints[i + 1], cubicPoints[i + 2], cubicPoints[i + 3], cubicPoints[i + 4], cubicPoints[i + 5]);
        }
    }

}
