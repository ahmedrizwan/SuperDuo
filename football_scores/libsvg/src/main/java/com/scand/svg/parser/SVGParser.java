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

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Typeface;

import com.scand.svg.SVG;
import com.scand.svg.parser.support.GraphicsSVG;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
	Low-level SVG parser handles all possible sources (streams, bytes, files) and
	(with help of SVGHandler) parses SVG contents itself (SAX-based).

	For typical cases should not be used directly.

	See SVGHelper to operate with.
*/
public class SVGParser {
	
	public float width;
	public float height;
    public ExternalSupport mExternalSupport;
    public String baseHref;

    /**
     Detects if stream is GZip'ed and if yes - wraps around GZIPInputStream
    */
	public static InputStream wrapIfZipped(InputStream input) {
        try {
            PushbackInputStream pb = new PushbackInputStream(input, 2); //we need a pushbackstream to look ahead
            byte[] signature = new byte[2];
            pb.read(signature); //read the signature
            pb.unread(signature); //push back the signature to the stream
            if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) //check if matches standard gzip magic number
                return new GZIPInputStream(pb);
            else
                return pb;
        }
        catch(IOException eio) {
            return input;
        }
	}

	/**
		Parses SVG file itself, with the parameters:
		@param svgData - SVG content
		@param baseWidth - content expected width  (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param baseHeight- content expected height (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param reqWidth - width to scale (might be 0 if content width should be used)
		@param reqHeight- height to scale (might be 0 if content width should be used)
		@param scale - scale factor
		@param bKeepAspectRatio - indicates keep aspect ratio should be used if scale applied
		@param bCropImage - indicates crop should be used if scale applied
        @param href - base URL for relative paths for resources
	*/
    public SVG parse(InputStream svgData, int baseWidth, int baseHeight, int reqWidth, int reqHeight, float scale, boolean bKeepAspectRatio, boolean bCropImage, String href) throws SVGParseException {
        return parse(wrapIfZipped(svgData), baseWidth, baseHeight, reqWidth, reqHeight, scale, false, bKeepAspectRatio, bCropImage, href);
    }

	/**
		Parses SVG file itself, with the parameters:
		@param svgData - SVG content
		@param baseWidth - content expected width  (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param baseHeight- content expected height (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param reqWidth - width to scale (might be 0 if content width should be used)
		@param reqHeight- height to scale (might be 0 if content width should be used)
		@param scale - scale factor
		@param bKeepAspectRatio - indicates keep aspect ratio should be used if scale applied
		@param bCropImage - indicates crop should be used if scale applied
        @param href - base URL for relative paths for resources
	*/
    public SVG parse(String svgData, int baseWidth, int baseHeight, int reqWidth, int reqHeight, float scale, boolean bKeepAspectRatio, boolean bCropImage, String href) throws SVGParseException {
        return parse(new ByteArrayInputStream(svgData.getBytes()), baseWidth, baseHeight, reqWidth, reqHeight, scale, false, bKeepAspectRatio, bCropImage, href);
    }

    /**
    	Parses the content, then renders the parsed context into canvas. In normal cases should not be called directly.
		@param svgData - SVG content
		@param baseWidth - content expected width  (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param baseHeight- content expected height (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param reqWidth - width to scale (might be 0 if content width should be used)
		@param reqHeight- height to scale (might be 0 if content width should be used)
		@param scale - scale factor
		@param bKeepAspectRatio - indicates keep aspect ratio should be used if scale applied
		@param bCropImage - indicates crop should be used if scale applied
         @param href - base URL for relative paths for resources
		@param canvas - context where SVG will be rendered to.
    */
    public void render(String svgData, int x, int y, int baseWidth, int baseHeight,int reqWidth, int reqHeight, float scale, boolean bKeepAspectRatio, boolean bCropImage, String href, Canvas canvas) throws SVGParseException {
        render(new ByteArrayInputStream(svgData.getBytes()), x, y, baseWidth, baseHeight,reqWidth,reqHeight,scale,bKeepAspectRatio, bCropImage, href, canvas);
    }

    private SVG parse(InputStream in, int baseWidth, int baseHeight, int reqWidth, int reqHeight,float scale, boolean idleMode, boolean bKeepAspectRatio, boolean bCropImage, String href) throws SVGParseException {
        baseHref = href;
    	try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://xml.org/sax/features/validation", false);
		//	spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); (crush on android)
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            SVGHandler handler = new SVGHandler(baseWidth, baseHeight,reqWidth,reqHeight,scale,bKeepAspectRatio,bCropImage,this);
            handler.setIdleMode(idleMode);
            xr.setContentHandler(handler);
            xr.parse(new InputSource(in));

            SVG result = new SVG(handler.getImage(), handler.bounds);
            if (!Float.isInfinite(handler.limits.top)) {
                result.setLimits(handler.limits);
            }
            return result;
        } catch (Exception e) {
            throw new SVGParseException(e);
        }
    }

    /**
    	Parses the content, then renders the parsed context into canvas. In normal cases should not be called directly.
		@param  in - SVG content
		@param  baseWidth - content expected width  (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param  baseHeight- content expected height (might be 0 if preprocessing requested by SVGHelper::checkSVGSize())
		@param  reqWidth - width to scale (might be 0 if content width should be used)
		@param  reqHeight- height to scale (might be 0 if content width should be used)
		@param  scale - scale factor
		@param  bKeepAspectRatio - indicates keep aspect ratio should be used if scale applied
		@param  bCropImage - indicates crop should be used if scale applied
        @param href - base URL for relative paths for resources
		@param  canvas - context where SVG will be rendered to.
    */
    public void render(InputStream in, int x, int y, int baseWidth, int baseHeight, int reqWidth, int reqHeight,float scale,  boolean bKeepAspectRatio, boolean bCropImage, String href, Canvas canvas) throws SVGParseException {
        in = wrapIfZipped(in);
        baseHref = href;
    	try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://xml.org/sax/features/validation", false);
			//spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); (crush on android)
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            canvas.save();
            
            SVGHandler handler = new SVGHandler(x, y, baseWidth, baseHeight,reqWidth, reqHeight, scale,bKeepAspectRatio, bCropImage, canvas, this);
            handler.setIdleMode(false);
            xr.setContentHandler(handler);
            xr.parse(new InputSource(in));
            canvas.restore();

        } catch (Exception e) {
            throw new SVGParseException(e);
        }
    }

    /**
    	Parses the header of SVG file to find the image sizes.
    	@param in - source
    	@param baseWidth - expected viewport (0, 0, baseWidth, baseHeight) should be used if none defied in SVG file.
    	@param baseHeight- expected viewport (0, 0, baseWidth, baseHeight) should be used if none defied in SVG file.
    */
    public void preparse(InputStream in, int baseWidth, int baseHeight) throws SVGParseException {
        in = wrapIfZipped(in);
        baseHref = null;
    	try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setFeature("http://xml.org/sax/features/validation", false);
			//spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); crush on android
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();

            SVGHandler handler = new SVGHandler(0, 0, baseWidth, baseHeight,0,0,1, true, false,null, this);
            handler.setIdleMode(true);
            xr.setContentHandler(handler);
            xr.parse(new InputSource(in));
            
            width = handler.width;
            if ( width < 2 && handler.limits != null ) {
                width = handler.limits.right - handler.limits.left;
            }
            height = handler.height;
            if ( height < 2 && handler.limits != null ) {
                height = handler.limits.bottom - handler.limits.top;
            }
            
        } catch (Exception e) {
            throw new SVGParseException(e);
        }
    }

    /**
    	Registers external easset manager for xlink: support.
    */
    public void registerAssetManager(AssetManager assetManager){
        mExternalSupport=new ExternalSupport(assetManager);
    }

    /**
    	Internal method handling fonts. Made as public and placed here to let you intercept 
    	the fonts management by simple override of the SVGParser rather providing your complete 
    	SVGHandler implementation. IdleMode means preprocessing to compute sizes by SVG content.
    */
    public void setFont( GraphicsSVG device,
                         String fontName, Float fontSize, String fontStyle, String fontWeight,
                         String text, boolean idleMode,
                         String extUrl) {
        if ( idleMode ) {
            return;
        }
        int style = Typeface.NORMAL;
        boolean hasFontStyle = false;
        if (fontWeight != null || fontStyle != null) {
            hasFontStyle = true;
            if ("italic".equalsIgnoreCase(fontStyle)) {
                style += Typeface.ITALIC;
            }
            if ("bold".equalsIgnoreCase(fontWeight)) {
                style += Typeface.BOLD;
            }
            try {
                int w = Integer.parseInt(fontWeight);
                if (w > 400) {
                    style += Typeface.BOLD;
                }
            } catch (NumberFormatException e) {
            }
        }

        String[] fontFamily=null;
        if (fontName!=null){
            fontFamily= fontName.split(",");
        }

        if (fontFamily == null && extUrl != null)
            fontFamily = new String[]{ extUrl };
        else if (fontFamily != null && extUrl != null) {
            String[] nff = new String[fontFamily.length + 1];
            nff[0] = extUrl;
            for(int j = 0; j < fontFamily.length; ++j) nff[j+1] = fontFamily[j];
            fontFamily = nff;
        }

        if ( fontFamily == null) {
            if (fontSize != null && hasFontStyle) {
                device.deriveFont(style, fontSize.intValue(), mExternalSupport);
            } else if (hasFontStyle) {
                device.deriveFont(style, mExternalSupport);
            } else if (fontSize != null) {
                device.deriveFont(fontSize);
            } else {
                device.deriveFont();
            }
        } else {
            if (fontSize != null && hasFontStyle) {
                device.setFont(fontFamily, style, fontSize.intValue(),mExternalSupport);
            } else if (hasFontStyle) {
                device.setFont(fontFamily, style, device.getFontSize(),mExternalSupport);
            } else if (fontSize != null) {
                device.setFont(fontFamily, device.getFontStyle(), fontSize.intValue(),mExternalSupport);
            } else {
                device.setFont(fontFamily, device.getFontStyle(), device.getFontSize(),mExternalSupport);
            }
        }
    }

    /**
    	Just debug function; doing nothing. Fill it with your content if necessary.
    */
    public static void log( String tag, String msg ) {
    	//System.out.println( tag + ": " + msg );
    }
}
