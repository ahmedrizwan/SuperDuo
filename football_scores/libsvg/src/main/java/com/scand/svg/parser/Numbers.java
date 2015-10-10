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

import java.util.ArrayList;

/**
	Yet another parser of the float numbers sequence but mixed with commands.
*/
class Numbers {
    ArrayList<Float> numbers;
    private int nextCmd;
    public float[] numberArray;

    public Numbers(ArrayList<Float> numbers, int nextCmd) {
        this.numbers = numbers;
        this.nextCmd = nextCmd;
        if ( numbers != null ) {
        	numberArray = new float[numbers.size()];
        	for ( int i = 0; i < numbers.size(); i++ ) {
        		numberArray[i] = numbers.get(i);
        	}
        } else {
        	numberArray = new float[0];
        }
    }

    public int getNextCmd() {
        return nextCmd;
    }

    public Float getNumber(int index) {
        return numbers.get(index);
    }

    public static Numbers parseNumbers(String s) {

    	int n = s.length();
        int p = 0;
        ArrayList<Float> numbers = new ArrayList<>();
        boolean skipChar = false;
        for (int i = 1; i < n; i++) {
            if (skipChar) {
                skipChar = false;
                continue;
            }
            char c = s.charAt(i);
            switch (c) {
            	case 'e':
            		skipChar = true;
            		break;
                case 'M':
                case 'm':
                case 'Z':
                case 'z':
                case 'L':
                case 'l':
                case 'H':
                case 'h':
                case 'V':
                case 'v':
                case 'C':
                case 'c':
                case 'S':
                case 's':
                case 'Q':
                case 'q':
                case 'T':
                case 't':
                case 'a':
                case 'A':
                case ')': {
                    String str = s.substring(p, i);
                    if (str.trim().length() > 0) {
                        //Util.debug("  Last: " + str);
                        Float f = Float.valueOf(str);
                        numbers.add(f);
                    }
                    p = i;
                    return new Numbers(numbers, p);
                }
                case '\n':
                case '\t':
                case ' ':
                case ',':
                case '-': {
                    String str = s.substring(p, i);
                    if (str.trim().length() > 0) {
                        Float f = Float.valueOf(str);
                        numbers.add(f);
                        if (c == '-') {
                            p = i;
                        } else {
                            p = i + 1;
                            skipChar = true;
                        }
                    } else {
                        p++;
                    }
                    break;
                }
            }
        }
        String last = s.substring(p);
        if (last.length() > 0) {
            //Util.debug("  Last: " + last);
            try {
                numbers.add(Float.valueOf(last));
            } catch (NumberFormatException nfe) {
                // Just white-space, forget it
            }
            p = s.length();
        }
        return new Numbers(numbers, p);
    }
}