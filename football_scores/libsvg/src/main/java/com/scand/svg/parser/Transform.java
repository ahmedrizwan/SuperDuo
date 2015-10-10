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

/**
	Parses SVG transform description to <em>java.awt.AffineTransform</em> android.graphics.Matrix.
*/
public class Transform {
	public static Matrix parseTransform(String s) {

        Matrix result = new Matrix();

		
		if (s == null) {
			return result;
		}
		
		do {
			s = s.trim();
			int cl = s.indexOf(')');
			if ( cl < 0 ) {
				return result;
			}
			if (s.startsWith("matrix(")) {
				Numbers np = Numbers.parseNumbers(s.substring("matrix(".length(), cl));
				if (np.numbers.size() == 6) {
					float[] ctm = new float[] {
							np.numberArray[0],
							np.numberArray[2],
                            np.numberArray[4],
							np.numberArray[1],
							np.numberArray[3],
                            np.numberArray[5],
							0,
							0,
                            1};
                    Matrix matrix = new Matrix();
                    matrix.setValues(ctm);
					result.preConcat(matrix);
				}
			} else if (s.startsWith("translate(")) {
				Numbers np = Numbers.parseNumbers(s.substring("translate(".length(), cl));
				if (np.numbers.size() > 0) {
					float tx = np.numberArray[0];
					float ty = 0;
					if (np.numbers.size() > 1) {
						ty = np.numberArray[1];
					}
					Matrix matrix = new Matrix();
					matrix.setTranslate(tx, ty);
					result.preConcat(matrix);
				}
			} else if (s.startsWith("scale(")) {
				Numbers np = Numbers.parseNumbers(s.substring("scale(".length(), cl));
				if (np.numbers.size() > 0) {
					float sx = np.numberArray[0];
					float sy = sx;
					if (np.numbers.size() > 1) {
						sy = np.numberArray[1];
					}
                    Matrix matrix = new Matrix();
					matrix.setScale(sx, sy);
					//result.postConcat(matrix);
                    result.preConcat(matrix);
				}
			} else if (s.startsWith("skewX(")) {
				Numbers np = Numbers.parseNumbers(s.substring("skewX(".length(), cl));
				if (np.numbers.size() > 0) {
					float angle = np.numberArray[0];
					angle = (float) Math.toRadians(angle);
                    Matrix matrix = new Matrix();
					matrix.setSkew((float) Math.tan(angle), 0);
					result.preConcat(matrix);
				}
			} else if (s.startsWith("skewY(")) {
				Numbers np = Numbers.parseNumbers(s.substring("skewY(".length(), cl));
				if (np.numbers.size() > 0) {
					float angle = np.numberArray[0];
					angle = (float) Math.toRadians(angle);
                    Matrix matrix = new Matrix();
					matrix.setSkew(0, (float) Math.tan(angle));
					result.preConcat(matrix);
				}
			} else if (s.startsWith("rotate(")) {
				Numbers np = Numbers.parseNumbers(s.substring("rotate(".length(), cl));
				if (np.numbers.size() > 0) {
					float angle = np.numberArray[0];
					angle = (float) Math.toRadians(angle);
					float cx = 0;
					float cy = 0;
					if (np.numbers.size() > 2) {
						cx = np.numberArray[1];
						cy = np.numberArray[2];
					}

					if (cx != 0 || cy != 0) {
                        Matrix m1 = new Matrix();
						m1.setTranslate(cx, cy);
                        Matrix m2 = new Matrix();
						m2.setRotate(angle);
                        Matrix m3 = new Matrix();
						m3.setTranslate(-cx, -cy);
						m1.preConcat(m2);
						m1.preConcat(m3);
						return m1;
					} else {
						Matrix matrix = new Matrix();
						matrix.setRotate(angle);
						result.preConcat(matrix);
					}
				}
			}
			
			s = s.substring(cl + 1);
			s = s.trim();
			if ( "".equals(s) ) {
				break;
			}
			if ( s.startsWith(",") ) {
				s = s.substring(1);
				s = s.trim();
			}

			cl = s.indexOf(')');
			if ( cl < 0 ) {
				break;
			}
		} while ( true );

		return result;
	}
}
