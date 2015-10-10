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

import com.scand.svg.parser.support.ColorSVG;

import java.util.HashMap;
import java.util.Iterator;

/**
	Predfefined colors table. Internal use only.
*/
public class Colors {

	public static HashMap COLOR_MAP = new HashMap();

	public static boolean isColor( String color ) {
		if (color == null) {
			return false;
		}
		color = color.toLowerCase();
		for ( int i = 0; i < CSS_COLORS.length; i++ ) {
			if ( CSS_COLORS[i].equals( color ) ) {
				return true;
			}
		}
		return false;
	}

	public static ColorSVG getColor( String color ) {
		if (color == null) {
			return null;
		}
		color = color.toLowerCase();
		for ( int i = 0; i < CSS_COLORS.length; i++ ) {
			if ( CSS_COLORS[i].equals( color ) ) {
				Integer ci = (Integer)COLOR_MAP.get(color);
				return new ColorSVG(ci.intValue());
			}
		}
		return null;
	}

	private static String[] CSS_COLORS;
	static {
		if ( COLOR_MAP.isEmpty() ) {
			COLOR_MAP.put( "aliceblue", new Integer( 0xfff0f8ff ) );
			COLOR_MAP.put( "antiquewhite", new Integer( 0xfffaebd7 ) );
			COLOR_MAP.put( "aqua", new Integer( 0xff00ffff ) );
			COLOR_MAP.put( "aquamarine", new Integer( 0xff7fffd4 ) );
			COLOR_MAP.put( "azure", new Integer( 0xfff0ffff ) );
			COLOR_MAP.put( "beige", new Integer( 0xfff5f5dc ) );
			COLOR_MAP.put( "bisque", new Integer( 0xffffe4c4 ) );
			COLOR_MAP.put( "black", new Integer( 0xff000000 ) );
			COLOR_MAP.put( "blanchedalmond", new Integer( 0xffffebcd ) );
			COLOR_MAP.put( "blue", new Integer( 0xff0000ff ) );
			COLOR_MAP.put( "blueviolet", new Integer( 0xff8a2be2 ) );
			COLOR_MAP.put( "brown", new Integer( 0xffa52a2a ) );
			COLOR_MAP.put( "burlywood", new Integer( 0xffdeb887 ) );
			COLOR_MAP.put( "cadetblue", new Integer( 0xff5f9ea0 ) );
			COLOR_MAP.put( "chartreuse", new Integer( 0xff7fff00 ) );
			COLOR_MAP.put( "chocolate", new Integer( 0xffd2691e ) );
			COLOR_MAP.put( "coral", new Integer( 0xffff7f50 ) );
			COLOR_MAP.put( "cornflowerblue", new Integer( 0xff6495ed ) );
			COLOR_MAP.put( "cornsilk", new Integer( 0xfffff8dc ) );
			COLOR_MAP.put( "crimson", new Integer( 0xffdc143c ) );
			COLOR_MAP.put( "cyan", new Integer( 0xff00ffff ) );
			COLOR_MAP.put( "darkblue", new Integer( 0xff00008b ) );
			COLOR_MAP.put( "darkcyan", new Integer( 0xff008b8b ) );
			COLOR_MAP.put( "darkgoldenrod", new Integer( 0xffb8860b ) );
			COLOR_MAP.put( "darkgray", new Integer( 0xffa9a9a9 ) );
			COLOR_MAP.put( "darkgrey", new Integer( 0xffa9a9a9 ) );
			COLOR_MAP.put( "darkgreen", new Integer( 0xff006400 ) );
			COLOR_MAP.put( "darkkhaki", new Integer( 0xffbdb76b ) );
			COLOR_MAP.put( "darkmagenta", new Integer( 0xff8b008b ) );
			COLOR_MAP.put( "darkolivegreen", new Integer( 0xff556b2f ) );
			COLOR_MAP.put( "darkorange", new Integer( 0xffff8c00 ) );
			COLOR_MAP.put( "darkorchid", new Integer( 0xff9932cc ) );
			COLOR_MAP.put( "darkred", new Integer( 0xff8b0000 ) );
			COLOR_MAP.put( "darksalmon", new Integer( 0xffe9967a ) );
			COLOR_MAP.put( "darkseagreen", new Integer( 0xff8fbc8f ) );
			COLOR_MAP.put( "darkslateblue", new Integer( 0xff483d8b ) );
			COLOR_MAP.put( "darkslategray", new Integer( 0xff2f4f4f ) );
			COLOR_MAP.put( "darkslategrey", new Integer( 0xff2f4f4f ) );
			COLOR_MAP.put( "darkturquoise", new Integer( 0xff00ced1 ) );
			COLOR_MAP.put( "darkviolet", new Integer( 0xff9400d3 ) );
			COLOR_MAP.put( "deeppink", new Integer( 0xffff1493 ) );
			COLOR_MAP.put( "deepskyblue", new Integer( 0xff00bfff ) );
			COLOR_MAP.put( "dimgray", new Integer( 0xff696969 ) );
			COLOR_MAP.put( "dimgrey", new Integer( 0xff696969 ) );
			COLOR_MAP.put( "dodgerblue", new Integer( 0xff1e90ff ) );
			COLOR_MAP.put( "firebrick", new Integer( 0xffb22222 ) );
			COLOR_MAP.put( "floralwhite", new Integer( 0xfffffaf0 ) );
			COLOR_MAP.put( "forestgreen", new Integer( 0xff228b22 ) );
			COLOR_MAP.put( "fuchsia", new Integer( 0xffff00ff ) );
			COLOR_MAP.put( "gainsboro", new Integer( 0xffdcdcdc ) );
			COLOR_MAP.put( "ghostwhite", new Integer( 0xfff8f8ff ) );
			COLOR_MAP.put( "gold", new Integer( 0xffffd700 ) );
			COLOR_MAP.put( "goldenrod", new Integer( 0xffdaa520 ) );
			COLOR_MAP.put( "gray", new Integer( 0xff808080 ) );
			COLOR_MAP.put( "grey", new Integer( 0xff808080 ) );
			COLOR_MAP.put( "green", new Integer( 0xff008000 ) );
			COLOR_MAP.put( "greenyellow", new Integer( 0xffadff2f ) );
			COLOR_MAP.put( "honeydew", new Integer( 0xfff0fff0 ) );
			COLOR_MAP.put( "hotpink", new Integer( 0xffff69b4 ) );
			COLOR_MAP.put( "indianred", new Integer( 0xffcd5c5c ) );
			COLOR_MAP.put( "indigo", new Integer( 0xff4b0082 ) );
			COLOR_MAP.put( "ivory", new Integer( 0xfffffff0 ) );
			COLOR_MAP.put( "khaki", new Integer( 0xfff0e68c ) );
			COLOR_MAP.put( "lavender", new Integer( 0xffe6e6fa ) );
			COLOR_MAP.put( "lavenderblush", new Integer( 0xfffff0f5 ) );
			COLOR_MAP.put( "lawngreen", new Integer( 0xff7cfc00 ) );
			COLOR_MAP.put( "lemonchiffon", new Integer( 0xfffffacd ) );
			COLOR_MAP.put( "lightblue", new Integer( 0xffadd8e6 ) );
			COLOR_MAP.put( "lightcoral", new Integer( 0xfff08080 ) );
			COLOR_MAP.put( "lightcyan", new Integer( 0xffe0ffff ) );
			COLOR_MAP.put( "lightgoldenrodyellow", new Integer( 0xfffafad2 ) );
			COLOR_MAP.put( "lightgray", new Integer( 0xffd3d3d3 ) );
			COLOR_MAP.put( "lightgrey", new Integer( 0xffd3d3d3 ) );
			COLOR_MAP.put( "lightgreen", new Integer( 0xff90ee90 ) );
			COLOR_MAP.put( "lightpink", new Integer( 0xffffb6c1 ) );
			COLOR_MAP.put( "lightsalmon", new Integer( 0xffffa07a ) );
			COLOR_MAP.put( "lightseagreen", new Integer( 0xff20b2aa ) );
			COLOR_MAP.put( "lightskyblue", new Integer( 0xff87cefa ) );
			COLOR_MAP.put( "lightslategray", new Integer( 0xff778899 ) );
			COLOR_MAP.put( "lightslategrey", new Integer( 0xff778899 ) );
			COLOR_MAP.put( "lightsteelblue", new Integer( 0xffb0c4de ) );
			COLOR_MAP.put( "lightyellow", new Integer( 0xffffffe0 ) );
			COLOR_MAP.put( "lime", new Integer( 0xff00ff00 ) );
			COLOR_MAP.put( "limegreen", new Integer( 0xff32cd32 ) );
			COLOR_MAP.put( "linen", new Integer( 0xfffaf0e6 ) );
			COLOR_MAP.put( "magenta", new Integer( 0xffff00ff ) );
			COLOR_MAP.put( "maroon", new Integer( 0xff800000 ) );
			COLOR_MAP.put( "mediumaquamarine", new Integer( 0xff66cdaa ) );
			COLOR_MAP.put( "mediumblue", new Integer( 0xff0000cd ) );
			COLOR_MAP.put( "mediumorchid", new Integer( 0xffba55d3 ) );
			COLOR_MAP.put( "mediumpurple", new Integer( 0xff9370d8 ) );
			COLOR_MAP.put( "mediumseagreen", new Integer( 0xff3cb371 ) );
			COLOR_MAP.put( "mediumslateblue", new Integer( 0xff7b68ee ) );
			COLOR_MAP.put( "mediumspringgreen", new Integer( 0xff00fa9a ) );
			COLOR_MAP.put( "mediumturquoise", new Integer( 0xff48d1cc ) );
			COLOR_MAP.put( "mediumvioletred", new Integer( 0xffc71585 ) );
			COLOR_MAP.put( "midnightblue", new Integer( 0xff191970 ) );
			COLOR_MAP.put( "mintcream", new Integer( 0xfff5fffa ) );
			COLOR_MAP.put( "mistyrose", new Integer( 0xffffe4e1 ) );
			COLOR_MAP.put( "moccasin", new Integer( 0xffffe4b5 ) );
			COLOR_MAP.put( "navajowhite", new Integer( 0xffffdead ) );
			COLOR_MAP.put( "navy", new Integer( 0xff000080 ) );
			COLOR_MAP.put( "oldlace", new Integer( 0xfffdf5e6 ) );
			COLOR_MAP.put( "olive", new Integer( 0xff808000 ) );
			COLOR_MAP.put( "olivedrab", new Integer( 0xff6b8e23 ) );
			COLOR_MAP.put( "orange", new Integer( 0xffffa500 ) );
			COLOR_MAP.put( "orangered", new Integer( 0xffff4500 ) );
			COLOR_MAP.put( "orchid", new Integer( 0xffda70d6 ) );
			COLOR_MAP.put( "palegoldenrod", new Integer( 0xffeee8aa ) );
			COLOR_MAP.put( "palegreen", new Integer( 0xff98fb98 ) );
			COLOR_MAP.put( "paleturquoise", new Integer( 0xffafeeee ) );
			COLOR_MAP.put( "palevioletred", new Integer( 0xffd87093 ) );
			COLOR_MAP.put( "papayawhip", new Integer( 0xffffefd5 ) );
			COLOR_MAP.put( "peachpuff", new Integer( 0xffffdab9 ) );
			COLOR_MAP.put( "peru", new Integer( 0xffcd853f ) );
			COLOR_MAP.put( "pink", new Integer( 0xffffc0cb ) );
			COLOR_MAP.put( "plum", new Integer( 0xffdda0dd ) );
			COLOR_MAP.put( "powderblue", new Integer( 0xffb0e0e6 ) );
			COLOR_MAP.put( "purple", new Integer( 0xff800080 ) );
			COLOR_MAP.put( "red", new Integer( 0xffff0000 ) );
			COLOR_MAP.put( "rosybrown", new Integer( 0xffbc8f8f ) );
			COLOR_MAP.put( "royalblue", new Integer( 0xff4169e1 ) );
			COLOR_MAP.put( "saddlebrown", new Integer( 0xff8b4513 ) );
			COLOR_MAP.put( "salmon", new Integer( 0xfffa8072 ) );
			COLOR_MAP.put( "sandybrown", new Integer( 0xfff4a460 ) );
			COLOR_MAP.put( "seagreen", new Integer( 0xff2e8b57 ) );
			COLOR_MAP.put( "seashell", new Integer( 0xfffff5ee ) );
			COLOR_MAP.put( "sienna", new Integer( 0xffa0522d ) );
			COLOR_MAP.put( "silver", new Integer( 0xffc0c0c0 ) );
			COLOR_MAP.put( "skyblue", new Integer( 0xff87ceeb ) );
			COLOR_MAP.put( "slateblue", new Integer( 0xff6a5acd ) );
			COLOR_MAP.put( "slategray", new Integer( 0xff708090 ) );
			COLOR_MAP.put( "slategrey", new Integer( 0xff708090 ) );
			COLOR_MAP.put( "snow", new Integer( 0xfffffafa ) );
			COLOR_MAP.put( "springgreen", new Integer( 0xff00ff7f ) );
			COLOR_MAP.put( "steelblue", new Integer( 0xff4682b4 ) );
			COLOR_MAP.put( "tan", new Integer( 0xffd2b48c ) );
			COLOR_MAP.put( "teal", new Integer( 0xff008080 ) );
			COLOR_MAP.put( "thistle", new Integer( 0xffd8bfd8 ) );
			COLOR_MAP.put( "tomato", new Integer( 0xffff6347 ) );
			COLOR_MAP.put( "turquoise", new Integer( 0xff40e0d0 ) );
			COLOR_MAP.put( "violet", new Integer( 0xffee82ee ) );
			COLOR_MAP.put( "wheat", new Integer( 0xfff5deb3 ) );
			COLOR_MAP.put( "white", new Integer( 0xffffffff ) );
			COLOR_MAP.put( "windowtext", new Integer( 0xff0 ) );
			COLOR_MAP.put( "window", new Integer( 0xffffffff ) );
			COLOR_MAP.put( "whitesmoke", new Integer( 0xfff5f5f5 ) );
			COLOR_MAP.put( "yellow", new Integer( 0xffffff00 ) );
			COLOR_MAP.put( "yellowgreen", new Integer( 0xff9acd32 ) );
			
			CSS_COLORS = new String[COLOR_MAP.size()];
			Iterator ii = COLOR_MAP.keySet().iterator();
			int i = 0;
			while (ii.hasNext()) {
				String color = (String)ii.next();
				CSS_COLORS[i++] = color;
			}
		}
	}
}
