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

/**
	Simple parser of float sequences like java.util.Scanner. 
*/
public class FloatSequence {

    private char ch;
    private CharSequence s;
    public int pos;
    private int n;

    public FloatSequence(CharSequence s, int pos) {
        this.s = s;
        this.pos = pos;
        n = s.length();
        ch = s.charAt(pos);
    }

    private char read() {
        if (pos < n) ++pos;
        if (pos == n) return '\0';
        return s.charAt(pos);
    }

    public void skipWhitespace() {
        while (pos < n) {
            if (!Character.isWhitespace(s.charAt(pos))) break;
            advance();
        }
    }

    public void skipNumberSeparator() {
        while (pos < n) {
            char c = s.charAt(pos);
            if(c!= ',' && !Character.isWhitespace(c)) break;
            advance();
        }
    }

    public void advance() {
        ch = read();
    }

    private char getc() {
    	return ch;
    }

    private float parseNumber() {
    	float r = 0;
    	char cc;
    	while(Character.isDigit(cc = getc())) {
    		r = r * 10 + ((int)cc - (int)'0') % 10;
    		advance();
    	}
    	return r;
    }

    /**
     * Parses the content of the buffer and converts it to a float.
     */
    public float parseFloat() {
        float     mant     = 0;
        float     mantDig  = 0;
        boolean mantPos  = true;
        boolean expPos   = true;

        int startPos, endPos;

        /* -12.24e-2 */
        char cc = getc();
        if(cc == '-') mantPos = false;
        if(cc == '+' || cc == '-') advance();

        mant = parseNumber();

        cc = getc();
        if(cc == '.') {
        	advance();
        	startPos = pos;
        	mantDig = parseNumber();
        	endPos = pos;
        	if(startPos != endPos)
	        	mant += mantDig / pow10[endPos - startPos];
        }
        if(!mantPos) mant = -mant;

        cc = getc();
        if(cc == 'e' || cc=='E') {
        	advance();
        	cc = getc();
        	if(cc == '-') expPos = false;
        	if(cc == '-' || cc == '+') advance();

        	int exp = (int)parseNumber();
        	if(!expPos) exp = - exp;

        	if (exp < -125 || mant == 0) return 0.0f;
        	if (exp >=  128) return mant > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        	if (exp == 0) return mant;

        	mant = (float)(exp > 0 ? mant * pow10[exp] : mant / pow10[-exp]);
        }
        return mant;


    }

    private void reportUnexpectedCharacterError(char c) {
        throw new RuntimeException("Unexpected char '" + c + "'.");
    }

    /**
     * Array of powers of ten. Using double instead of float gives a tiny bit more precision.
     */
    private static final double[] pow10 = new double[128];

    static {
        for (int i = 0; i < pow10.length; i++) {
            pow10[i] = Math.pow(10, i);
        }
    }

    public float nextFloat() {
        skipWhitespace();
        float f = parseFloat();
        skipNumberSeparator();
        return f;
    }
}
