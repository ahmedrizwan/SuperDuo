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
import com.scand.svg.css.InlineRule;

import org.xml.sax.Attributes;

import java.util.StringTokenizer;

/**
	CSS properties assignable to any SVG element.
*/
class Properties {
    Attributes attributes;
    InlineRule style;
    
    public String toString() {
    	String str = "";
    	for ( int i = 0; i < attributes.getLength(); i++ ) {
    		str += attributes.getQName(i) + "=" + attributes.getValue(i);
    		if ( i < attributes.getLength() - 1 ) {
    			str += ", ";
    		}
    	}
    	return str;
    }

    public Properties(Attributes atts, InlineRule style) {
        this.attributes = atts;
        this.style = style;
    }

    public String getProperty(String name) {
        String v = null;
        if (style != null) {
            Object o = style.get(name);
            if ( o != null ) {
            	v = o.toString();
            }
        }
        if (v == null) {
            v = getStringAttr(name);
        }
        return v;
    }

    public String getString(String name) {
        return getProperty(name);
    }

    public Integer getHex(String name) {
        String v = getProperty(name);
        if (v == null || !v.startsWith("#")) {
            return null;
        } else {
            try {
                return new Integer(Integer.parseInt(v.substring(1), 16));
            } catch (NumberFormatException nfe) {
                // todo - parse word-based color here
                return null;
            }
        }
    }

    public ColorSVG getOpacityColorForGradient(String name, PaintData c){
        String prefix = "";
        Float parentOpacity = null;
        if ( name.startsWith("fill") ) {
            prefix = "fill-";
            parentOpacity = (c != null ? c.fillOpacity : null);
        }
        if ( name.startsWith("stroke") ) {
            prefix = "stroke-";
            parentOpacity = (c != null ? c.strokeOpacity : null);
        }
        if ( name.startsWith("stop") ) {
            prefix = "stop-";
        }

        Float globOpacity = getFloat("opacity");
        if (globOpacity == null && c!=null){
            globOpacity = c.opacity;
        }

        Float opacity = getFloat(prefix + "opacity");
        if (opacity == null) {
            if( parentOpacity == null ) {
                opacity = globOpacity;
            } else {
                opacity = parentOpacity*(globOpacity!=null?globOpacity:1);
            }
        } else {
            opacity = opacity*(globOpacity!=null?globOpacity:1);
        }

        int alphaModifier = 0;
        int alphaInt=255;//By default
        if (opacity != null) {
            alphaInt = Math.round(255 * opacity);
        }
        alphaModifier = (alphaInt << 24);

        String v = getProperty(name);

        return new ColorSVG( alphaModifier, true );
    }

    public ColorSVG getColor(String name, PaintData c) {
    	
    	String prefix = "";
    	Float parentOpacity = null;
    	ColorSVG parentColor = null;
    	if ( name.startsWith("fill") ) {
    		prefix = "fill-";
    		parentOpacity = (c != null ? c.fillOpacity : null);
    		parentColor =  (c != null ? c.fillColor : null);
    	}
    	if ( name.startsWith("stroke") ) {
    		prefix = "stroke-";
    		parentOpacity = (c != null ? c.strokeOpacity : null);
    		parentColor =  (c != null ? c.strokeColor : null);
    	}
    	if ( name.startsWith("stop") ) {
    		prefix = "stop-";
    	}

        Float globOpacity = getFloat("opacity");
        if (globOpacity == null && c!=null){
            globOpacity = c.opacity;
        }

        Float opacity = getFloat(prefix + "opacity");
        if (opacity == null) {
        	if( parentOpacity == null ) {
                opacity = globOpacity;
            } else {
                opacity = parentOpacity*(globOpacity!=null?globOpacity:1);
        	}
        } else {
            opacity = opacity*(globOpacity!=null?globOpacity:1);
        }
        
        int alphaModifier = 0;
        if (opacity != null) {
            int alphaInt = Math.round(255 * opacity);
            alphaModifier = (alphaInt << 24);
        }
    	
        String v = getProperty(name);

        if ("none".equalsIgnoreCase(v)){
            return ColorSVG.createNoneColor();
        }
        
        if (v == null || !v.startsWith("#")) {
                if ( opacity != null ) {
        		ColorSVG col = null;
        		if ( v == null ) {
        			col = parentColor;
        		} else {
        			col = Properties.getNamedColor(v);
        		}
        		if ( col == null ) {
                    if (alphaModifier!=0 && name.startsWith("fill")){
                        return new ColorSVG(alphaModifier, true);//situation when we have opacity without color
                    }else {
                        return null;
                    }
        		} else if (!col.isNone) {
                    int rgb = col.getRGB() & 0xFFFFFF;
                    return new ColorSVG(rgb | alphaModifier, true);
                } else {
                    return col;//returns "none" color
                }
        	} else {
        		if ( v == null ) {
        			return parentColor;
        		} else {
        			return Properties.getNamedColor(v);
        		}
        	}
        } else {
            try {
            	v = v.substring(1).trim();
            	if ( v.length() == 3 ) {
            		char r = v.charAt(0); 
            		char g = v.charAt(1); 
            		char b = v.charAt(2); 
            		v = "" + r + r + g + g + b + b;  
            	}
                int i = Integer.parseInt(v, 16);
            	if ( opacity != null ) {
                    ColorSVG col=new ColorSVG(i | alphaModifier, true);
                    return col;
            	} else {

                    if (v.length() == 6){
                        return new ColorSVG(i , false);
                    } else{
                        return new ColorSVG(i);
                    }
            	}
            } catch (NumberFormatException nfe) {
                return Properties.getNamedColor(v);
            }
        }
    }
    
    public Float getFloat(String name, Float defaultValue) {
        String v = getProperty(name);
        if (v == null) {
            return defaultValue;
        } else {
        	float factor = 1;
            if (v.endsWith("in")) {
            	factor = 96;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("px")) {
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("pt")) {
                factor=1.3f;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("cm")) {
                //factor = 37.795f;
                factor= 35.43307f;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("mm")) {
                //	factor = 370.795f;
                factor = 3.543307f;//from spec
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("em")) {
            	factor = 16f; // XXX
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("%")) {
            	factor = .01f;
                v = v.substring(0, v.length() - 1);
            }
            v=v.replace(" ","");// for prevent crush when float like "45464 54654 5464"
            float ff = Float.parseFloat(v);
            return new Float(ff * factor);
        }
    }

    public Float getFloat(String name) {
    	return getFloat(name, null);
    }
    

    /* Attributes only, no style */
    
    public Numbers getNumberParseAttr(String name) {
        String num = getProperty(name);
        if (num!=null) {
            return Numbers.parseNumbers(num);
        } else {
            return null;
        }
    }

    public String getStringAttr(String name) {
    	return getStringAttr(name, null);
    }
    
    public String getStringAttr(String name, String defaultValue) {
        int n = attributes.getLength();
        for (int i = 0; i < n; i++) {
            if (attributes.getLocalName(i).equals(name)) {
                return attributes.getValue(i);
            }
        }
        return defaultValue;
    }

    public Float getFloatAttr(String name) {
        return getFloatAttr(name, null);
    }

    public Float getScalledFloatAttr(String name, Float baseValue) {
    	if ( attributes == null ) {
            return baseValue;
    	}
    	
        String v = getStringAttr(name);
        if (v == null) {
            return baseValue;
        } else {
        	float factor = 1;
            if (v.endsWith("in")) {
            	factor = 96;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("px")) {
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("pt")) {
                factor=1.25f;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("cm")) {
            	//factor = 37.795f;
                factor= 35.43307f;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("mm")) {
            //	factor = 370.795f;
                factor = 3.543307f;//from spec
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("em")) {
            	factor = 16f; // XXX
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("%")) {
                v = v.substring(0, v.length() - 1);
                return new Float(Float.parseFloat(v) * .01f * baseValue);
            }
            float ff;
			try {
				ff = Float.parseFloat(v);
			} catch (NumberFormatException e) {
				int iz1 = v.indexOf(',');
				int iz2 = v.indexOf(' ');
				int iz = -1;
				if ( iz1 >= 0 ) {
					if ( iz2 > 0 ) {
						iz = Math.min(iz1, iz2);
					} else {
						iz = iz1;
					}
					iz1 = -1;
					iz2 = -1;
				}
				if ( iz2 >= 0 ) {
					if ( iz1 > 0 ) {
						iz = Math.min(iz1, iz2);
					} else {
						iz = iz2;
					}
				}
				if ( iz > 0 ) {
					ff = Float.parseFloat(v.substring(0, iz));
				} else {
					throw e;
				}
			}
            
            return new Float(ff * factor);
        }
    }

    
    
    public Float getFloatAttr(String name, Float defaultValue) {
    	if ( attributes == null ) {
            return defaultValue;
    	}
        String v = getStringAttr(name);
        if (v == null) {
            return defaultValue;
        } else {
        	
        	float factor = 1;
        	
            if (v.endsWith("in")) {
            	factor = 96;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("px")) {
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("pt")) {
                factor=1.3f;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("cm")) {
                //factor = 37.795f;
                factor= 35.43307f;
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("mm")) {
                //	factor = 370.795f;
                factor = 3.543307f;//from spec
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("em")) {
            	factor = 16f; // XXX
                v = v.substring(0, v.length() - 2);
            }
            if (v.endsWith("%")) {
            	factor = .01f;
                v = v.substring(0, v.length() - 1);
            }
            
            int ind = v.indexOf(' ');
            if ( ind > 0 ) {
            	v = v.substring(0, ind);
            }
            float ff;
			try {
				ff = Float.parseFloat(v);
			} catch (NumberFormatException e) {
				int iz1 = v.indexOf(',');
				int iz2 = v.indexOf(' ');
				int iz = -1;
				if ( iz1 >= 0 ) {
					if ( iz2 > 0 ) {
						iz = Math.min(iz1, iz2);
					} else {
						iz = iz1;
					}
					iz1 = -1;
					iz2 = -1;
				}
				if ( iz2 >= 0 ) {
					if ( iz1 > 0 ) {
						iz = Math.min(iz1, iz2);
					} else {
						iz = iz2;
					}
				}
				if ( iz > 0 ) {
					ff = Float.parseFloat(v.substring(0, iz));
				} else {
					throw e;
				}
			}
            return new Float(ff * factor);
        }
    }

    public Integer getHexAttr(String name) {
        String v = getStringAttr(name);
        if (v == null) {
            return null;
        } else {
            try {
                return new Integer(Integer.parseInt(v.substring(1), 16));
            } catch (NumberFormatException nfe) {
                return null;
            }
        }
    }

    public static ColorSVG getNamedColor( String colorName ) {

    	if ( colorName == null ) {
    		return null;
    	}
    	
    	if ( colorName.indexOf("rgb") >= 0 ) {
    		int i1 = colorName.indexOf("(");
    		int i2 = colorName.indexOf(")");
    		if ( i2 > i1 ) {
    			String col = colorName.substring(i1+1, i2);
    			if ( col != null && col.length() > 0 ) {
    				StringTokenizer st = new StringTokenizer(col, ",");
    				if ( st.countTokens() == 3 ) {
    					int r = Integer.parseInt(st.nextToken().trim());
    					int g = Integer.parseInt(st.nextToken().trim());
    					int b = Integer.parseInt(st.nextToken().trim());
    					return new ColorSVG(r, g, b);
    				}
    			}
    			return null;
    		}
    		return null;
    	}

    	return Colors.getColor(colorName);
    }
}