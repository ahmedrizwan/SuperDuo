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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Base64;
import android.util.Log;

import com.scand.svg.parser.paintutil.ShaderApply;
import com.scand.svg.parser.support.BasicStroke;
import com.scand.svg.parser.support.ClipPath;
import com.scand.svg.parser.support.ColorSVG;
import com.scand.svg.parser.support.EllipseSVG;
import com.scand.svg.parser.support.GraphicsSVG;
import com.scand.svg.parser.support.ImageRefSVG;
import com.scand.svg.parser.support.LineSVG;
import com.scand.svg.parser.support.PathExt;
import com.scand.svg.parser.support.RectangleSVG;
import com.scand.svg.css.CSSParser;
import com.scand.svg.css.CSSStylesheet;
import com.scand.svg.css.CascadeEngine;
import com.scand.svg.css.CascadeResult;
import com.scand.svg.css.ElementProperties;
import com.scand.svg.css.InlineRule;
import com.scand.svg.css.util.SMap;
import com.scand.svg.css.util.SMapAttributesAdapter;
import com.scand.svg.css.util.SMapImpl;
import com.scand.svg.parser.paintutil.FilterImpl;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
	SAX Handler doing whole parsing process.
*/
public class SVGHandler extends DefaultHandler {

    //private static final String LOG_TAG = "Handler";

    private int left;
    private int top;
    private int baseWidth;
    private int baseHeight;
    private int reqWidth;
    private int reqHeight;
    private GraphicsSVG g;
    private float scaleX;
    private float scaleY;

    Bitmap picture;
    GraphicsSVG canvas;

    RectF rect = new RectF();
    RectF bounds = null;

    boolean idleMode = false;

    private boolean hidden = false;
    private int hiddenLevel = 0;
    private boolean boundsMode;
    private boolean patternDefinition;
    private boolean metaData;
    private boolean defs;

    private Stack<PaintData> ctxs = new Stack<>();
    private Stack<Boolean> gpushed = new Stack<>();

    private boolean runStyle = false;
    private String style = "";

    private Text currentText;

    HashMap<String, Gradient> gradientMap = new HashMap<>();
    HashMap<String, Gradient> gradientRefMap = new HashMap<>();
    HashMap<String, ClipPath> clipMap = new HashMap<>();
    Gradient gradient = null;

    private Vector<CSSStylesheet> stylesheets = new Vector<>();
    private CascadeEngine cascadeEngine = new CascadeEngine();
    private CSSParser cssParser = new CSSParser();
    public float width;
    public float height;
    private float ww;
    private float hh;

    private boolean bKeepAspectRatio = true;
    private boolean bCropImage = false;

    private SVGParser parser;
    private boolean specialGraphicsMode;

    private int movedOriginX = 0;
    private int movedOriginY = 0;

    // defs parsings
    HashMap<String, StringBuffer> defx = new HashMap<>();
    StringBuffer sbx = new StringBuffer();

    int svgNesting = 0;
    boolean recursion;


    // filters parsing
    private Filter curFilter = null;
    private Stack<FilterOp> feStack = new Stack<>();
    private HashMap<String, Filter> filters = new HashMap<>();

    RectF limits = new RectF(
            Float.POSITIVE_INFINITY,
            Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY,
            Float.NEGATIVE_INFINITY);

    SVGHandler(int baseWidth, int baseHeight, int requestWidth, int requestHeight, float scale, boolean bKeepAspectRatio, boolean bCropImage, SVGParser parser) {
        this.baseWidth = baseWidth;
        this.baseHeight=baseHeight;
        this.reqWidth = requestWidth;
        this.reqHeight=requestHeight;
        this.parser = parser;
        this.scaleX=scale;
        this.scaleY=scale;
        this.bKeepAspectRatio=bKeepAspectRatio;
        this.bCropImage=bCropImage;
    }

    public SVGHandler(int x, int y, int baseWidth, int baseHeight, int requestWidth, int requestHeight, float scale, boolean bKeepAspectRatio, boolean bCropImage,Canvas g, SVGParser parser) {
        this.left = x;
        this.top = y;
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.reqWidth = requestWidth;
        this.reqHeight=requestHeight;
        this.g = new GraphicsSVG(g);
        this.scaleX = scale;
        this.scaleY=scale;
        this.parser = parser;
        this.bCropImage = bCropImage;
        this.bKeepAspectRatio=bKeepAspectRatio;
    }

    Bitmap getImage() {
        return picture;
    }

    public void setIdleMode(boolean mode) {
        this.idleMode = mode;
    }

    public void startDocument() throws SAXException {
        // Set up prior to parsing a doc
    }

    public void endDocument() throws SAXException {
        // Clean up after parsing a doc
    }

    private void applyFilterChain(GraphicsSVG canvas, PaintData c, boolean stroke, RectF bounds) {
        for(FilterOp fop : c.filter.op) {
            FilterImpl impl = Filter.buildFilterImpl(fop);
            if(impl != null)
                impl.handle(fop, canvas, stroke, bounds);
            else
                Log.w("SVGKit", fop.filterOp + ": not implemented");
        }
    }

    private boolean setFill(Properties atts, PaintData c, HashMap gradients, RectF bounds) {
        //Log.d(LOG_TAG, "setFill");
        if ("none".equals(atts.getString("display"))) {
            return false;
        }

        String v = atts.getProperty("fill");
        if ("none".equals(v)) {
            return false;
        }

        if (idleMode) {
            return true;
        }

        if(c.filter != null)
            applyFilterChain(canvas, c, false, bounds);

        String fillString = atts.getString("fill");
        if (fillString != null && fillString.startsWith("url(#")) {
            int end = fillString.indexOf(")");
            String id = fillString.substring("url(#".length(), end);
            Gradient gr = (Gradient) gradients.get(id);
            if (gr != null) {
                try {
                   // Log.d(LOG_TAG, "set gradient " + gr.id);
                    ColorSVG alphaColor= atts.getOpacityColorForGradient("fill", parentContext());
                    ShaderApply.applyShader(gr, bounds.left, bounds.top, bounds.width(), bounds.height(), canvas, alphaColor);
                } catch (Throwable e) { // NoClassdefFoundError
                    e.printStackTrace();
                }
                return true;
            } else {
             //   Log.d(LOG_TAG, "gradient not found " + id + " size " + gradients.size());
                return false;
            }
        } else {
            ColorSVG color = atts.getColor("fill", parentContext());
            if (color != null) {
                if ( color.getAlpha() == 0 || color.isNone) {
                    return false;
				}
                canvas.setFillColor(color.mValue);
                return true;
            } else if ((atts.getString("fill") == null) /*&& (atts.getString("stroke") == null)*/) {
                canvas.setFillColor(android.graphics.Color.BLACK);
                //canvas.setStrokeColor(android.graphics.Color.BLACK);
                return true;
            }
        }
        return false;
    }

    private PaintData getContext(Properties atts, PaintData c, HashMap gradients) {
        ColorSVG color = atts.getColor("stroke", c);
        if (color == null && c != null) {
            color = c.strokeColor;
        }
        Float width = atts.getFloat("stroke-width");
        if (width == null && c != null) {
            width = c.strokeWidth;
        }

        float lineWidth = 0;
        Paint.Cap capStyle = Paint.Cap.BUTT;
        Paint.Join joinStyle = Paint.Join.MITER;

        /* by spec default stroke="none"
        if (color == null && width != null && width.floatValue() > 0) {
            color = new ColorSVG(0);
            color.mValue = android.graphics.Color.BLACK;
        }*/

        if (color == null){
            color = ColorSVG.createNoneColor();
        }

        if (width != null) {
            lineWidth = width;
        } else {
            if (color != null) {
                lineWidth = 2;
            }
        }

        String linecap = atts.getString("stroke-linecap");
        if (linecap == null && c != null) {
            capStyle = c.strokeCapStyle;
        } else {
            if ("round".equals(linecap)) {
                capStyle = Paint.Cap.ROUND;
            } else if ("square".equals(linecap)) {
                capStyle = Paint.Cap.SQUARE;
            } else if ("butt".equals(linecap)) {
                capStyle = Paint.Cap.BUTT;
            }
        }

        String linejoin = atts.getString("stroke-linejoin");
        if (linecap == null && c != null) {
            joinStyle = c.strokeJoinStyle;
        } else {
            if ("miter".equals(linejoin)) {
                joinStyle = Paint.Join.MITER;
            } else if ("round".equals(linejoin)) {
                joinStyle = Paint.Join.ROUND;
            } else if ("bevel".equals(linejoin)) {
                joinStyle = Paint.Join.BEVEL;
            }
        }

        PaintData ctx = new PaintData();

        ctx.fontName = atts.getString("font-family");
        if (ctx.fontName == null && c != null) {
            ctx.fontName = c.fontName;
        }
        ctx.fontSize = atts.getFloat("font-size", null);
        if (ctx.fontSize == null && c != null) {
            ctx.fontSize = c.fontSize;
        }
        ctx.fontWeight = atts.getString("font-weight");
        if (ctx.fontWeight == null && c != null) {
            ctx.fontWeight = c.fontWeight;
        }
        ctx.fontStyle = atts.getString("font-style");
        if (ctx.fontStyle == null && c != null) {
            ctx.fontStyle = c.fontStyle;
        }
        ctx.textAnchor = atts.getString("text-anchor");
        if (ctx.textAnchor == null && c != null) {
            ctx.textAnchor = c.textAnchor;
        }

        if (width != null || lineWidth > 0) {
            ctx.strokeWidth = lineWidth;
        }
        ctx.strokeCapStyle = capStyle;
        ctx.strokeJoinStyle = joinStyle;
        ctx.strokeColor = color;

        String fillString = atts.getString("fill");
        if (fillString != null && fillString.startsWith("url(#")) {
            String id = fillString.substring("url(#".length(), fillString.length() - 1);
            ctx.gr = (Gradient) gradients.get(id);
        } else {
            ctx.fillColor = atts.getColor("fill", parentContext());
        }

        ctx.opacity = atts.getFloat("opacity");
        if (ctx.opacity == null && c != null) {
            ctx.opacity = c.opacity;
        }

        ctx.fillOpacity = atts.getFloat("fill-opacity");
        if (ctx.fillOpacity == null && c != null) {
            ctx.fillOpacity = c.fillOpacity;
        }

        ctx.strokeOpacity = atts.getFloat("stroke-opacity");
        if (ctx.strokeOpacity == null && c != null) {
            ctx.strokeOpacity = c.strokeOpacity;
        }

        Numbers nn = atts.getNumberParseAttr("stroke-dasharray");
        if (nn != null && nn.numbers.size() > 0) {
            ctx.dasharray = nn;
        } else if (c != null) {
            ctx.dasharray = c.dasharray;
        }

        Float dashOffset = atts.getFloat("stroke-dashoffset");
        if (dashOffset != null) {
            ctx.dashOffset = dashOffset;
        } else if (c != null) {
            ctx.dashOffset = c.dashOffset;
        }

        String fref = atts.getStringAttr("filter");
        if (fref != null) {
            // now we supporting only form url(#id)
            fref = fref.trim();
            if (fref.startsWith("url(")) {
                int k = fref.indexOf('#');
                if (k > 0) {
                    fref = fref.substring(k + 1).replace(')', ' ').trim();
                    ctx.filter = filters.get(fref);
                }
            }
        }

        return ctx;
    }

    private BasicStroke setStroke(Properties atts, PaintData c,  HashMap gradients, RectF shaderRect) {
      //  Log.d(LOG_TAG, "setStroke");
        boolean gradient=false;
        if (idleMode) {
            return null;
        }

        if ("none".equals(atts.getString("display"))) {
            return null;
        }

        String v = atts.getProperty("stroke");
        if ("none".equals(v)) {
            return null;
        }

        if (v != null && v.startsWith("url(#")) {
            String id = v.substring("url(#".length(), v.length() - 1);
            Gradient gr = (Gradient) gradients.get(id);
            if (gr != null) {
                try {
                    // Log.d(LOG_TAG, "set gradient " + gr.id);
                    ColorSVG alphaColor= atts.getOpacityColorForGradient("stroke", parentContext());
                    ShaderApply.applyShader(gr, shaderRect.left, shaderRect.top, shaderRect.width(), shaderRect.height(), canvas, alphaColor);
                    gradient=true;
                } catch (Throwable e) { // NoClassdefFoundError
                    e.printStackTrace();
                }
            }
        }

        ColorSVG color = atts.getColor("stroke", parentContext());
        if (!gradient) {
            if (color == null && c != null) {
                color = c.strokeColor;
            }
            if (color == null || color.isNone) {
                return null; //By default stroke="none".
            }
        }
        Float width = atts.getFloat("stroke-width");
        if (width == null && c != null) {
            width = c.strokeWidth;
        }

        float lineWidth = 0;
        Paint.Cap capStyle = Paint.Cap.BUTT;
        Paint.Join joinStyle = Paint.Join.MITER;

		/*if (color == null && width != null && width > 0) {
            color = new Color(0);
			color.mValue = android.graphics.Color.BLACK;
		}*/

        if (width != null) {
            lineWidth = width;
        } else {
            if (color != null) {
                lineWidth = 2;
            }
        }

        if (color == null && !gradient) {
            color = new ColorSVG(0);
            color.mValue = android.graphics.Color.BLACK;
        }

        if(c.filter != null)
            applyFilterChain(canvas, c, true, shaderRect);

        if (color != null && lineWidth > 0) {

            String linecap = atts.getString("stroke-linecap");
            if (linecap == null && c != null) {
                capStyle = c.strokeCapStyle;
            } else {
                if ("round".equals(linecap)) {
                    capStyle = Paint.Cap.ROUND;
                } else if ("square".equals(linecap)) {
                    capStyle = Paint.Cap.SQUARE;
                } else if ("butt".equals(linecap)) {
                    capStyle = Paint.Cap.BUTT;
                }
            }

            String linejoin = atts.getString("stroke-linejoin");
            if (linecap == null && c != null) {
                joinStyle = c.strokeJoinStyle;
            } else {
                if ("miter".equals(linejoin)) {
                    joinStyle = Paint.Join.MITER;
                } else if ("round".equals(linejoin)) {
                    joinStyle = Paint.Join.ROUND;
                } else if ("bevel".equals(linejoin)) {
                    joinStyle = Paint.Join.BEVEL;
                }
            }

            Numbers nn = atts.getNumberParseAttr("stroke-dasharray");
            if (nn == null || nn.numbers.size() == 0) {
                if (c != null && !"none".equalsIgnoreCase(atts.getString("stroke-dasharray"))) {
                    nn = c.dasharray;
                }
            }

            Float dashOffset = atts.getFloat("stroke-dashoffset");
            if (dashOffset == null) {
                if (c != null && !"none".equalsIgnoreCase(atts.getString("stroke-dashoffset"))) {
                    dashOffset = c.dashOffset;
                }
            }

            BasicStroke stroke;
            if (nn == null || nn.numbers.size() == 0) {
                stroke = new BasicStroke(lineWidth, capStyle, joinStyle);
            } else {
                int size = nn.numbers.size();
                float[] dash = new float[size];
                for (int i = 0; i < size; i++) {
                    dash[i] = nn.getNumber(i);
                }
                stroke = new BasicStroke(lineWidth, capStyle, joinStyle, 1f, dash, dashOffset != null ? dashOffset : 0f);
            }

            if (!gradient && lineWidth>0)
                canvas.setStrokeColor(color.mValue);
            canvas.setStroke(stroke);

            return stroke;
        }
        return null;
    }

    private Gradient registerGradient(boolean isLinear, Properties pp) {

        if (idleMode) {
            return null;
        }

        Gradient gradient = new Gradient();

        gradient.id = pp.getStringAttr("id");
        gradient.isLinear = isLinear;
        if (isLinear) {
            gradient.x1 = pp.getFloatAttr("x1", 1f);//default value from spec
            gradient.x2 = pp.getFloatAttr("x2", 0f);
            gradient.y1 = pp.getFloatAttr("y1", 0f);
            gradient.y2 = pp.getFloatAttr("y2", 0f);

            gradient.pX1 = ("" + pp.getStringAttr("x1")).endsWith("%");
            gradient.pX2 = ("" + pp.getStringAttr("x2")).endsWith("%");
            gradient.pY1 = ("" + pp.getStringAttr("y1")).endsWith("%");
            gradient.pY2 = ("" + pp.getStringAttr("y2")).endsWith("%");

        } else {
            gradient.x = pp.getFloatAttr("cx", 0.5f);//default value from spec
            gradient.y = pp.getFloatAttr("cy", 0.5f);//default value from spec
            gradient.fx = pp.getFloatAttr("fx", 0.5f);
            gradient.fy = pp.getFloatAttr("fy", 0.5f);
            gradient.radius = pp.getFloatAttr("r", 0.5f);//default value from spec

            gradient.pX = ("" + pp.getStringAttr("cx")).endsWith("%");
            gradient.pY = ("" + pp.getStringAttr("cy")).endsWith("%");
            gradient.pFX = ("" + pp.getStringAttr("fx", "50%")).endsWith("%");
            gradient.pFY = ("" + pp.getStringAttr("fy", "50%")).endsWith("%");
            gradient.pR = ("" + pp.getStringAttr("r")).endsWith("%");
        }
        gradient.spreadMethod=pp.getStringAttr("spreadMethod","pad");
        gradient.gradientUnits=pp.getStringAttr("gradientUnits","objectBoundingBox");
        String transform = pp.getStringAttr("gradientTransform");
        if (transform != null) {
            gradient.matrix = Transform.parseTransform(transform);
        }
        String xlink = pp.getStringAttr("href");
        if (xlink != null) {
            if (xlink.startsWith("#")) {
                xlink = xlink.substring(1);
            }
            gradient.xlink = xlink;
        }
        return gradient;
    }

    private void updateLimits(float x, float y) {
        if (x + movedOriginX < limits.left) {
            limits.left = x + movedOriginX;
        }
        if (x + movedOriginX > limits.right) {
            limits.right = x + movedOriginX;
        }
        if (y + movedOriginY < limits.top) {
            limits.top = y + movedOriginY;
        }
        if (y + movedOriginY > limits.bottom) {
            limits.bottom = y + movedOriginY;
        }
    }

    private void updateLimits(RectF r2) {
        rect.left = r2.left;
        rect.right = r2.left + r2.width();
        rect.top = r2.top;
        rect.bottom = r2.left + r2.height();

        updateLimits(rect.left, rect.top);
        updateLimits(rect.right, rect.bottom);
    }

    private Boolean pushTransform(Properties pp) {

        final String transform = pp.getStringAttr("transform");
        final String clip = pp.getProperty("clip-path");
        if (transform == null && clip == null) {
            return Boolean.FALSE;
        }
        final Matrix matrix = Transform.parseTransform(transform);

        float[] values = new float[9];
        matrix.getValues(values);
        if (values[Matrix.MTRANS_X] != 0) {
            movedOriginX = (int) values[Matrix.MTRANS_X];
        }
        if (values[Matrix.MTRANS_Y] != 0) {
            movedOriginY = (int) values[Matrix.MTRANS_Y];
        }
        if (idleMode) {
            return Boolean.FALSE;
        }

        canvas.transform(matrix, doClipPath(pp, canvas));

        return Boolean.TRUE;
    }

    private void popTransform() {

        if (idleMode) {
            return;
        }

        if (specialGraphicsMode) {
            canvas.transform(null, null);
        } else {
            canvas.restore();
        }

    }

    private void picSizeRecalc(String widthStr, String heightStr, Properties pp, float rate){
        width = 0;
        height = 0;
        if (baseWidth > baseHeight){
            width = widthStr != null ? pp.getScalledFloatAttr("width", (float)baseWidth) : (float) baseWidth;

            if (heightStr != null) {
                Float h = pp.getScalledFloatAttr("height", 0f);
                if (h == 0)
                    height = (rate > 0) ? pp.getScalledFloatAttr("height", baseWidth / rate) : width;
                else
                    height = h;
            } else
                height = rate > 0 ? width / rate : width;
        } else {
            height = heightStr != null ? pp.getScalledFloatAttr("height", (float)baseHeight) : (float) baseHeight;

            if (widthStr != null) {
                Float w = pp.getScalledFloatAttr("width", 0f);
                if (w == 0)
                    width = rate > 0 ? pp.getScalledFloatAttr("width", (float)baseHeight / (1 / rate)) : height;
                else
                    width = w;
            } else
                width = rate > 0 ? height / (1/rate) : height;
        }
    }

    private void picTransformationRecalc(){
        if (reqWidth!=0 && reqHeight!=0) {
            if (!bKeepAspectRatio){
                scaleY = reqHeight / height;
                scaleX = reqWidth / width;
            } else if (bCropImage) {

                if (reqWidth<reqHeight){
                    scaleX=reqHeight/height;
                    if (scaleX * width < reqWidth) {
                        scaleX = reqWidth/ width;
                    }
                } else {
                    scaleX=reqWidth/width;
                    if (scaleX * height < reqHeight) {
                        scaleX = reqHeight / height;
                    }
                }
                scaleY=scaleX;

            } else {
                scaleX=calcScaleKeepAspectRatioNotCrop(width, height, reqWidth, reqHeight);
                scaleY=scaleX;
            }
        }
    }

    private float calcScaleKeepAspectRatioNotCrop(float width, float height, float reqWidth, float reqHeight){
        float scale;
        if (width < height) {
            scale = reqHeight/ height;
            if (scale * width > reqWidth) {
                scale = reqWidth/ width;
            }
        } else {
            scale = reqWidth / width;
            if (scale * height > reqHeight) {
                scale = reqHeight / height;
            }
        }
        return scale;
    }

    private Matrix parseSize(Properties pp, boolean forBitmap){

        // Float x = pp.getFloatAttr("x", 0f);
        // Float y = pp.getFloatAttr("y", 0f);

        String widthStr = pp.getStringAttr("width");
        String heightStr = pp.getStringAttr("height");
        float xx = -1;
        float yy = -1;
        ww = -1;
        hh = -1;
        float rate = 0;

        Numbers numbers = pp.getNumberParseAttr("viewBox");
        if (numbers != null) {
            try {
                xx = numbers.getNumber(0);
                yy = numbers.getNumber(1);
                ww = numbers.getNumber(2);
                hh = numbers.getNumber(3);
                if (hh > 0) {
                    rate = ww / hh;
                }
            } catch (Exception e) {
                xx = -1;
                yy = -1;
                ww = -1;
                hh = -1;
            }
        }

        picSizeRecalc(widthStr, heightStr, pp, rate);

        picTransformationRecalc();

        Matrix tx = null;

        if (!forBitmap) {

            if (scaleX!=1||scaleY!=1){
                tx = new Matrix();
                tx.postScale(scaleX, scaleY);
            }
            if (left != 0 || top != 0) {
                if (tx == null) {
                    tx = new Matrix();
                }
                tx.postTranslate(left, top);
            }

            if (numbers!=null) {
                //by spec default meetOrSlice=meet and align=xMidYMid
                Matrix tx2 = new Matrix();
                float scale= calcScaleKeepAspectRatioNotCrop(ww, hh, width, height); // meetOrSlice=meet
                tx2.setScale(scale, scale);
                xx*=scale;
                yy*=scale;

                //align=xMidYMid
                float xx1=(width - ww*scale)/2f + xx;
                float yy1=(height - hh*scale)/2f + yy;
                tx2.postTranslate(-(xx1),-(yy1));
                if (tx != null) {
                    tx.postConcat(tx2);
                } else {
                    tx = tx2;
                }
            }

        } else {
            if (numbers!=null) {
                //by spec default meetOrSlice=meet and align=xMidYMid
                tx = new Matrix();
                float scale= calcScaleKeepAspectRatioNotCrop(ww, hh, width, height);// meetOrSlice=meet
                tx.setScale(scale, scale);
                xx*=scale;
                yy*=scale;

                //align=xMidYMid
                float xx1=(width - ww*scale)/2f + xx;
                float yy1=(height - hh*scale)/2f + yy;
                tx.postTranslate(-(xx1),-(yy1));
            }

            if (/*applyExtraScale &&*/ scaleX != 1 || scaleY != 1) {
                height *= scaleY;
                width *= scaleX;
                //for resizing image when there are no viewBox
                if (tx == null) {
                    tx = new Matrix();
                }
                tx.postScale(scaleX, scaleY);
                //tx.setScale(scale, scale);
            }
            //Log.v("app", "apply scaleX " + scaleX + " scaleY " + scaleY + " width " + width + " height " + height);
        }

        if (width == 0) {
            width = 1f;
        }
        if (height == 0) {
            height = 1f;
        }
        return tx;

    }


    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts) throws SAXException {

        if (localName == null || localName.length() == 0) {
            localName = qName;
        }

        SMap attrMap = null;
        if (atts == null) {
            attrMap = new SMapImpl();
        } else {
            attrMap = new SMapAttributesAdapter(atts);
        }

        cascadeEngine.pushElement(getNamespace(), localName, attrMap);
        String style = atts.getValue("style");
        if (style != null) {
            InlineRule ir = cssParser.readInlineStyle(style);
            cascadeEngine.applyInlineRule(ir);
        }
        CascadeResult cascadeResult = cascadeEngine.getCascadeResult();

        ElementProperties ep = cascadeResult.getProperties();
        InlineRule elementStyle = ep.getPropertySet();

        boolean skip = hidden | patternDefinition | metaData | defs;

        String id = atts.getValue("id");
        if (id != null && !recursion) {
            if (localName.equals("g") || localName.equals("clipPath")) {
                if (defs) {
                    sbx = new StringBuffer();
                    defx.put(id, sbx);
                }
            } else if (localName.equals("filter")) {
                curFilter = new Filter();
                filters.put(id, curFilter);
                feStack.clear();
                curFilter.id = id;
            } else if (localName.equals("symbol")) {
                sbx = new StringBuffer();
                defx.put(id, sbx);
                sbx.append("<g>\n");
            } else {
                StringBuffer sx = new StringBuffer();
                dumpTag(sx, true, localName, atts);
                dumpTag(sx, false, localName, null);
                defx.put(id, sx);
            }
        }

        if (!recursion && defs) {
            dumpTag(sbx, true, localName, atts);
        }

        Properties pp = new Properties(atts, elementStyle);
        if (boundsMode) {
            if (localName.equals("rect")) {
                Float x = pp.getFloatAttr("x");
                if (x == null) {
                    x = 0f;
                }
                Float y = pp.getFloatAttr("y");
                if (y == null) {
                    y = 0f;
                }
                Float width = pp.getFloatAttr("width", 0f);
                Float height = pp.getFloatAttr("height", 0f);
                bounds = new RectF(x, y, x + width, y + height);
            }
            return;
        }

        if (localName.equals("svg")) {
            svgNesting++;

            if (svgNesting > 1) {
                return;
            }

            gpushed.push(pushTransform(pp));
            PaintData c = getContext(pp, parentContext(), gradientMap);
            ctxs.push(c);

            if (g == null) {

                Matrix tx=parseSize(pp, true);

                if (!idleMode) {
                    this.picture = Bitmap.createBitmap((int) width, (int) height, Bitmap.Config.ARGB_8888);
                    canvas = new GraphicsSVG(picture);

                    canvas.fillRect(0, 0, (int) width, (int) height);

                    canvas.setFont(null, 0, 16, null);

                    if (tx != null) {
                        canvas.setTransform(tx);
                        //savedTx.push(tx);
                    }
                }

            } else {
                Matrix tx=parseSize(pp, false);

                if (!idleMode) {
                    canvas = g;
                    canvas.fillRect(0, 0, (int) width, (int) height);
                    canvas.setFont(null, 0, 16, null);
                   // parser.setFont(canvas, "Times New Roman", new Float(16), null, null, "", idleMode);

                    if (tx == null) {
                        tx = new Matrix();
                    }
                    canvas.setTransform(tx);
                }
            }

        } else if (!skip && localName.equals("tspan")) {
            if (currentText != null) {
                currentText.addSpan(new TextProperties(pp, parentContext()));
            }
        } else if (!skip && localName.equals("text")) {
            gpushed.push(pushTransform(pp));
            currentText = new Text(new TextProperties(pp, parentContext()));
        } else if (localName.equals("use")) {
            String href = atts.getValue("xlink:href");
            if(href == null) href = pp.getString("xlink:href");
            if(href == null) href = pp.getString("href");
            if(href == null) href = pp.getString("src");
            if(href == null) href = "";

            href = href.trim();
            if (href.startsWith("#")) {

                if (!recursion && !defs && !idleMode) {
                    recursion = true;
                    StringBuffer stb = defx.get(href.substring(1));

                    if (stb != null) {
//						System.out.println("["+stb.toString()+"]");
                        gpushed.push(pushTransform(pp));
                        PaintData c = getContext(pp, parentContext(), gradientMap);
                        ctxs.push(c);

                        Float x = pp.getFloatAttr("x");
                        if (x == null) {
                            x = 0f;
                        }
                        Float y = pp.getFloatAttr("y");
                        if (y == null) {
                            y = 0f;
                        }
                        canvas.postTranslate(x.intValue(), y.intValue());
                        try {
                            StringReader in = new StringReader(stb.toString());
                            SAXParserFactory spf = SAXParserFactory.newInstance();
                            spf.setFeature("http://xml.org/sax/features/validation", false);
                            //	spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false); crush on android
                            SAXParser sp = spf.newSAXParser();
                            XMLReader xr = sp.getXMLReader();
                            xr.setContentHandler(this);
                            xr.parse(new InputSource(in));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        canvas.postTranslate(-x.intValue(), -y.intValue());
                        ctxs.pop();
                        if (gpushed.pop()) {
                            popTransform();
                        }
                    }
                    recursion = false;
                }
            }
        } else if (localName.equals("symbol")) {
            defs = true;
        } else if (localName.equals("defs")) {
            defs = true;
        } else if (localName.equals("linearGradient")) {
            gradient = registerGradient(true, pp);
        } else if (localName.equals("radialGradient")) {
            gradient = registerGradient(false, pp);
        } else if (localName.equals("stop")) {
            if (gradient != null) {
                Float offset = pp.getFloat("offset");
                ColorSVG color = pp.getColor("stop-color", parentContext());
                if (color == null) {
                    color = new ColorSVG(0);
                    color.mValue = android.graphics.Color.BLACK;
                }
                if (offset != null) {
                    gradient.positions.add(offset);
                }
                gradient.colors.add(color);
            }
        } else if (localName.equals("g")) {
            if ("bounds".equalsIgnoreCase(id)) {
                boundsMode = true;
            }
            if (hidden) {
                hiddenLevel++;
            }
            if ("none".equals(pp.getString("display")) || "hidden".equals(pp.getString("visibility"))) {
                if (!hidden) {
                    hidden = true;
                    hiddenLevel = 1;
                }
            }
            gpushed.push(pushTransform(pp));
            PaintData c = getContext(pp, parentContext(), gradientMap);
            ctxs.push(c);
        } else if (!skip && localName.equals("rect")) {

            RectangleSVG rectangleSVG = parseRect(pp, false);

            gpushed.push(pushTransform(pp));
            updateLimits(rectangleSVG);

            if (idleMode) {
                return;
            }

            if (rectangleSVG != null && setFill(pp, parentContext(), gradientMap, rectangleSVG)) {

                canvas.fill(rectangleSVG);
            }

            rectangleSVG = parseRect(pp, true);
            if (setStroke(pp, parentContext(),gradientMap, rectangleSVG) != null) {
                canvas.draw(rectangleSVG);
            }
            if (gpushed.pop()) {
                popTransform();
            }
        } else if (!skip && localName.equals("line")) {

            LineSVG lineSVG = parseLine(pp);
            if (lineSVG != null) {
                updateLimits(lineSVG.bounds);

                if (idleMode) {
                    return;
                }

                if (setStroke(pp, parentContext(),gradientMap,lineSVG.bounds) != null) {
                    gpushed.push(pushTransform(pp));
                    canvas.draw(lineSVG);
                    if (gpushed.pop()) {
                        popTransform();
                    }
                }
            }
            if (idleMode) {
                return;
            }

        } else if (!skip && localName.equals("circle")) {
            EllipseSVG e2D = parseCircle(pp);
            if (e2D != null) {
                gpushed.push(pushTransform(pp));
                updateLimits(e2D.bounds);

                if (idleMode) {
                    return;
                }

                if (setFill(pp, parentContext(), gradientMap, e2D.ellipse)) {

                    canvas.fill(e2D);
                }


                if (setStroke(pp, parentContext(),gradientMap, e2D.bounds) != null) {
                    canvas.draw(e2D);
                }

                if (gpushed.pop()) {
                    popTransform();
                }
            }
        } else if (!skip && localName.equals("ellipse")) {
            EllipseSVG e2D = parseEllipse(pp);
            if (e2D != null) {

                gpushed.push(pushTransform(pp));
                updateLimits(e2D.bounds);

                if (idleMode) {
                    return;
                }

                if (setFill(pp, parentContext(), gradientMap, e2D.ellipse)) {
                    canvas.fill(e2D);
                }

                if (setStroke(pp, parentContext(),gradientMap, e2D.bounds) != null) {
                    canvas.draw(e2D);
                }

                if (gpushed.pop()) {
                    popTransform();
                }
            }
        } else if (!skip && (localName.equals("polygon") || localName.equals("polyline"))) {
            PathExt p = parsePolygon(pp, localName.equals("polygon"));
            gpushed.push(pushTransform(pp));
            if (p != null) {
                RectF bounds = new RectF();
                p.computeBounds(bounds, false);
                updateLimits(bounds);

                if (idleMode) {
                    return;
                }

                if (setFill(pp, parentContext(), gradientMap, bounds)) {
                    canvas.fill(p);
                }

                if (setStroke(pp, parentContext(),gradientMap,bounds) != null) {
                    canvas.draw(p);
                }

                if (gpushed.pop()) {
                    popTransform();
                }

            }
        } else if (!skip && localName.equals("path")) {
            PathExt p = PathSvg.parsePath(pp.getStringAttr("d"));

            RectF bounds = new RectF();
            p.computeBounds(bounds, false);
            updateLimits(bounds);

            if (idleMode) {
                return;
            }

            gpushed.push(pushTransform(pp));
          //  Log.d(""," style "+style);
       //     Log.d(""," style "+style+" "+(parentContext()!=null?parentContext().opacity:0)+" "+(parentContext()!=null?parentContext().fillOpacity:0)+" "+(parentContext()!=null?parentContext().strokeOpacity:0));
            if (setFill(pp, parentContext(), gradientMap, bounds)) {
                canvas.fill(p);
            }


            if (setStroke(pp, parentContext(),gradientMap, bounds) != null) {
                canvas.draw(p);
            }

            if (gpushed.pop()) {
                popTransform();
            }
        } else if (!skip && localName.equals("image")) {
            ImageRefSVG imageSVG = parseImage(pp);
            updateLimits(imageSVG.bounds);

            boolean dontDrawImage = false;
            if (idleMode) return;

            gpushed.push(pushTransform(pp));
            if ("none".equals(pp.getString("display")))
                dontDrawImage = true;
            else {
                // first we need apply colors; just to be sure it will not have alpha == 0.
                if(setStroke(pp, parentContext(), gradientMap, imageSVG.bounds) == null)
                    canvas.setStrokeColor(Color.BLACK);
            }

            if(!dontDrawImage)
                canvas.draw(imageSVG);

            if (gpushed.pop()) {
                popTransform();
            }
        } else if (localName.equals("title") || localName.equals("desc") || localName.equals("metadata")) {
            metaData = true;
        } else if (localName.equals("style")) {
            runStyle = true;
        } else if (!skip && localName.equals("pattern")) {
            patternDefinition = true;
        }
        else if (localName.equals("filter")) {
            if(curFilter != null) {
                curFilter.filterUnits = pp.getString("filterUnits");
            }
        } else if (Filter.isFilterOp(localName)) {
            FilterOp op = new FilterOp(localName);
            op.parseFrom(pp);
            if(!feStack.empty())
                feStack.peek().op.add(op);
            else
                curFilter.op.add(op);
            feStack.push(op);
        }

        else if (!skip && localName.length() > 0) {
            Log.w("SVGKit", "unknown command: '" + localName + "'");
        }
    }

    private PaintData parentContext() {
        return ctxs.size() > 0 ? (PaintData) ctxs.peek() : null;
    }

    public void characters(char ch[], int start, int length) {

        if (idleMode) {
            return;
        }


        String str = new String(ch, start, length);

        if (defs && !recursion) {
            sbx.append(str);
        }

        if (currentText != null) {
            currentText.setText(str);
        }
        if (runStyle) {
            style += str;
        }
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {


        if (localName == null || localName.length() == 0) {
            localName = qName;
        }

        // CascadeResult cascade = cascadeEngine.getCascadeResult();
        cascadeEngine.popElement();

        boolean skip = hidden | patternDefinition | metaData | defs;

        if (!recursion) {
            if (localName.equals("symbol")) {
                sbx.append("</g>");
            } else {
                if (defs && !localName.equals("defs") && !localName.equals("filter")) {
                    dumpTag(sbx, false, localName, null);
                }
            }
        }


        if (localName.equals("svg")) {

            svgNesting--;

        } else if (localName.equals("defs") || localName.equals("symbol")) {
            if (!recursion) {
                defs = false;
                sbx = new StringBuffer();
            }

            for (String id : gradientRefMap.keySet()) {
                Gradient gradient = gradientRefMap.get(id);
                if (gradient.xlink != null) {
                    Gradient parent = (Gradient) gradientRefMap.get(gradient.xlink);
                    if (parent != null) {
                        gradient = parent.createChild(gradient);
                   //     Log.d(LOG_TAG, "rebuilded gradient " + gradient.id + " size " + gradientRefMap.size() + " refmap " + gradientMap.size());
                    }
                }
                gradientMap.put(gradient.id, gradient);
                gradientRefMap.put(gradient.id, gradient);
            }

        } else if (!skip && localName.equals("tspan")) {
            if (currentText != null) {
                currentText.endSpan();
            }
        } else if (!skip && localName.equals("text")) {
            if (currentText != null) {

                PaintData co = (ctxs.size() > 0 ? (PaintData) ctxs.peek() : new PaintData());

                float x = 0;
                float y = 0;
                Iterator ii = currentText.spans.iterator();
                while (ii.hasNext()) {
                    TextSpan span = (TextSpan) ii.next();

                    if (!idleMode) {

                        ColorSVG fillColor = span.props.fillColor == null ?
                                (currentText.props.fillColor == null ? co.fillColor : currentText.props.fillColor) : span.props.fillColor;

                        if (fillColor != null)
                            canvas.setTextColor(fillColor.mValue);
                        else
                            canvas.setTextColor(android.graphics.Color.BLACK);
                    }
                    if (span.props.x != null)
                        x = span.props.x;
                    if (span.props.y != null)
                        y = span.props.y;

                    // does not work with text. a workaround would be with glyph
                    // vector.
                    // if ( span.props.strokeColor != null &&
                    // span.props.strokeWidth != null ) {
                    // int capStyle = BasicStroke.CAP_SQUARE;
                    // int joinStyle = BasicStroke.JOIN_MITER;
                    // BasicStroke stroke = new BasicStroke( 2, capStyle,
                    // joinStyle );
                    // canvas.setStroke(stroke);
                    // }

                    String textAnchor = span.props.textAnchor == null ?
                            (currentText.props.textAnchor == null ? co.textAnchor : currentText.props.textAnchor) : span.props.textAnchor;

                    String fontStyle = span.props.fontStyle == null ?
                            (currentText.props.fontStyle == null ? co.fontStyle : currentText.props.fontStyle) : span.props.fontStyle;
                    String fontWeight = span.props.fontWeight == null ?
                            (currentText.props.fontWeight == null ? co.fontWeight : currentText.props.fontWeight) : span.props.fontWeight;
                    String fontName = span.props.fontName == null ?
                            (currentText.props.fontName == null ? co.fontName : currentText.props.fontName) : span.props.fontName;
                    Float fontSize = span.props.fontSize == null ?
                            (currentText.props.fontSize == null ? co.fontSize : currentText.props.fontSize) : span.props.fontSize;
                    String extUrl = span.props.extUrl == null ?
                            (currentText.props.extUrl == null ? null : currentText.props.extUrl) : span.props.extUrl;

                    if (idleMode) {
                        int len = span.text == null ? 0 : span.text.length();
                        updateLimits(new RectF(x, y, len * 10, y));
                        x += len * 10;
                    } else {
                        parser.setFont(canvas, fontName, fontSize, fontStyle, fontWeight, span.text, idleMode, extUrl);
                        if (span.text == null)
                            continue;

                        int strlen = canvas.stringWidth(span.text);

                        float dx = span.props.dx == null ? 0 : span.props.dx;
                        int corr = 0;
                        if ("middle".equalsIgnoreCase(textAnchor)) {
                            corr = strlen / 2;
                            dx /= 2;
                        } else if ("end".equalsIgnoreCase(textAnchor)) {
                            corr = strlen;
                            dx = 0;
                        }

                        float xx = x + dx - corr;
                        float yy = y + (span.props.dy == null ? 0 : span.props.dy.byteValue());
                        canvas.drawString(span.text, xx, yy);

                        x += strlen;
                    }
                }

                if (gpushed.pop()) {
                    popTransform();
                }
            }
        } else if (localName.equals("linearGradient")) {
            if (idleMode) {
                return;
            }
            if (gradient.id != null) {
                gradientMap.put(gradient.id, gradient);
                gradientRefMap.put(gradient.id, gradient);
              //  Log.d(LOG_TAG, "added gradient " + gradient.id + " size " + gradientRefMap.size() + " refmap " + gradientMap.size());
            }
        } else if (localName.equals("radialGradient")) {
            if (idleMode) {
                return;
            }
            if (gradient.id != null) {
                gradientMap.put(gradient.id, gradient);
                gradientRefMap.put(gradient.id, gradient);
               // Log.d(LOG_TAG, "added gradient " + gradient.id + " size " + gradientRefMap.size() + " refmap " + gradientMap.size());
            }
        } else if (localName.equals("title") || localName.equals("desc") || localName.equals("metadata")) {
            metaData = false;
        } else if (localName.equals("style")) {
            runStyle = false;
            // System.out.println(style);
            // parser.setCSSURLFactory(new FB2CSSURLFactory());
            try {
                CSSStylesheet stylesheet = cssParser.readStylesheet(new StringReader(style));
                if (stylesheet != null) {
                    stylesheets.add(stylesheet);
                }
                cascadeEngine.add(stylesheet, null);
                // fontLocator = new EmbeddedFontLocator(stylesheet,
                // fontLocator);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (localName.equals("pattern")) {
            patternDefinition = false;
        } else if (localName.equals("g")) {
            if (idleMode) {
                return;
            }
            if (boundsMode) {
                boundsMode = false;
            }
            // Break out of hidden mode
            if (hidden) {
                hiddenLevel--;
                if (hiddenLevel == 0) {
                    hidden = false;
                }
            }
            // Clear gradient map
            //	gradientMap.clear();
            ctxs.pop();
            if (gpushed.pop()) {
                popTransform();
            }
        }
        else if(localName.equals("filter")) {
            curFilter.optimize();
            curFilter = null;
            feStack.clear();
        }
        else if(!feStack.empty() && feStack.peek().filterOp.equals(localName)) {
            feStack.pop();
        }
        else if(Filter.isFilterOp(localName)) {
            Log.w("SVGKit", "wrong close tag for " + localName);
        }
    }

    private void dumpTag(StringBuffer sb, boolean open, String localName, Attributes atts) {
        sb.append("<");
        sb.append((open ? "" : "/")).append(localName);
        if (atts != null) {
            for (int i = 0; i < atts.getLength(); i++) {
                sb.append(" ").append(atts.getLocalName(i)).append("=\"").append(atts.getValue(i)).append("\"");
            }
        }
        sb.append(">\n");
    }

    private static String getNamespace() {
        return "http://www.w3.org/2000/svg";
    }

    public CascadeResult getStyles(String name, InlineRule inlineStyle, SMap attrs) {
        cascadeEngine.pushElement(getNamespace(), name, attrs);
        cascadeEngine.applyInlineRule(inlineStyle);
        CascadeResult cascade = cascadeEngine.getCascadeResult();
        cascadeEngine.popElement();
        return cascade;
    }

    public boolean isSpecialGraphicsMode() {
        return specialGraphicsMode;
    }

    public void setSpecialGraphicsMode(boolean specialGraphicsMode) {
        this.specialGraphicsMode = specialGraphicsMode;
    }

    public class SVGClipHandler extends DefaultHandler {

        ClipPath mGroup;
        GraphicsSVG canvas;

        public SVGClipHandler(GraphicsSVG c) {
            mGroup = new ClipPath();
            canvas = c;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (localName == null || localName.length() == 0) {
                localName = qName;
            }

            SMap attrMap = null;
            if (attributes == null) {
                attrMap = new SMapImpl();
            } else {
                attrMap = new SMapAttributesAdapter(attributes);
            }

            cascadeEngine.pushElement(getNamespace(), localName, attrMap);
            String style = attributes.getValue("style");
            if (style != null) {
                InlineRule ir = cssParser.readInlineStyle(style);
                cascadeEngine.applyInlineRule(ir);
            }
            CascadeResult cascadeResult = cascadeEngine.getCascadeResult();

            ElementProperties ep = cascadeResult.getProperties();
            InlineRule elementStyle = ep.getPropertySet();

            Properties pp = new Properties(attributes, elementStyle);

            /*if (localName.equals("clipPath")){
                gpushed.push(pushTransform(pp));
            } else*/
            if (localName.equals("rect")) {
                mGroup.addElement(parseRect(pp, true));
            } else if (localName.equals("circle")) {
                mGroup.addElement(parseCircle(pp));
            } else if (localName.equals("ellipse")) {
                mGroup.addElement(parseEllipse(pp));
            } else if (localName.equals("line")) {
                mGroup.addElement(parseLine(pp));
            } else if ((localName.equals("polygon") || localName.equals("polyline"))) {
                mGroup.addElement(parsePolygon(pp, localName.equals("polygon")));
            } else if (localName.equals("path")) {
                mGroup.addElement(parsePath(pp));
            }
        }
    }

    private ClipPath doClipPath(Properties pp, GraphicsSVG canvas) {
        if (idleMode) {
            return null;
        }

        String clipPathString = pp.getProperty("clip-path");
        if (clipPathString != null && clipPathString.startsWith("url(#")) {
            String id = clipPathString.substring("url(#".length(), clipPathString.length() - 1);
            ClipPath clipPath = clipMap.get(id);
            if (clipPath == null) {
                StringBuffer sub = defx.get(id);
                try {
                    clipPath = parseClipPath(sub, canvas);
                } catch (SAXException | ParserConfigurationException | IOException e) {
                    e.printStackTrace();
                }
                clipMap.put(id, clipPath);
            }
            return clipPath;
        } else {
            return null;
        }
    }

    private ClipPath parseClipPath(StringBuffer sub, GraphicsSVG canvas) throws SAXException, ParserConfigurationException, IOException {
        SVGClipHandler clipHandler = new SVGClipHandler(canvas);
        if (sub==null) return null;
        StringReader in = new StringReader(sub.toString());
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setFeature("http://xml.org/sax/features/validation", false);
        SAXParser sp = spf.newSAXParser();
        XMLReader xr = sp.getXMLReader();
        xr.setContentHandler(clipHandler);
        xr.parse(new InputSource(in));
        return clipHandler.mGroup;
    }

    private PathExt parsePath(Properties pp) {
        return PathSvg.parsePath(pp.getStringAttr("d"));
    }

    private PathExt parsePolygon(Properties pp, boolean polygon) {
        Numbers numbers = pp.getNumberParseAttr("points");
        PathExt p = null;
        if (numbers != null) {
            float[] points = numbers.numberArray;
            if (points.length > 1) {
                String fillRule = pp.getString("fill-rule");
                Path.FillType rule = Path.FillType.WINDING;
                if ("evenodd".equalsIgnoreCase(fillRule)) {
                    rule = Path.FillType.EVEN_ODD;
                }
                p = new PathExt();
                p.setFillType(rule);


                p.moveTo(points[0], points[1]);
                for (int i = 2; i < points.length; i += 2) {
                    float x = points[i];
                    float y = points[i + 1];
                    p.lineTo(x, y);
                }

                if (/*localName.equals("polygon")*/polygon) { // != "polyline"
                    p.close();
                }

                RectF bounds = new RectF();
                p.computeBounds(bounds, false);
            }
        }
        return p;
    }

    private EllipseSVG parseEllipse(Properties pp) {
        Float centerX = pp.getFloatAttr("cx");
        Float centerY = pp.getFloatAttr("cy");
        Float radiusX = pp.getFloatAttr("rx");
        Float radiusY = pp.getFloatAttr("ry");
        EllipseSVG e2D = null;
        if (centerX != null && centerY != null && radiusX != null && radiusY != null) {

            rect.set(centerX - radiusX,
                    centerY - radiusY,
                    centerX + radiusX,
                    centerY + radiusY);

            e2D = new EllipseSVG(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
        }
        return e2D;
    }

    private EllipseSVG parseCircle(Properties pp) {
        Float centerX = pp.getFloatAttr("cx");
        Float centerY = pp.getFloatAttr("cy");
        Float radius = pp.getFloatAttr("r");
        EllipseSVG e2D = null;
        if (centerX != null && centerY != null && radius != null) {

            e2D = new EllipseSVG(
                    centerX - radius,
                    centerY - radius, radius * 2,
                    radius * 2);
        }
        return e2D;
    }

    private LineSVG parseLine(Properties pp) {
        Float x1 = pp.getFloatAttr("x1");
        Float x2 = pp.getFloatAttr("x2");
        Float y1 = pp.getFloatAttr("y1");
        Float y2 = pp.getFloatAttr("y2");

        LineSVG lineSVG = null;
        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            lineSVG = new LineSVG(x1,y1,x2,y2);
        }
        return lineSVG;
    }

    /*
    <image x="0" y="0" clip-path="url(#clipPath25)" width="2573"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xlink:href="data:image/png;base64,iVBORw0...UVORK5CYII="
        xlink:type="simple" xlink:actuate="onLoad" height="2580" preserveAspectRatio="none" xlink:show="embed"
    />
    */
    private ImageRefSVG parseImage(Properties pp) {
        Float x = pp.getFloat("x", 0f);
        Float y = pp.getFloat("y", 0f);
        Float width = pp.getScalledFloatAttr("width", ww);
        Float height = pp.getScalledFloatAttr("height", hh);

        //Log.wtf("SVGKit", "Image["+x+","+y+","+width+","+height+"]");

        if(idleMode)
            return new ImageRefSVG(x, y, width, height);

        boolean noKeep = "none".equals(pp.getStringAttr("preserveAspectRatio", "xMidYMid"));

        String href = pp.getString("xlink:href");
        if(href == null) href = pp.getString("href");
        if(href == null) href = pp.getString("src");
        if(href == null) {
            Log.e("SVGKit", "href not found");
            return new ImageRefSVG(x, y, width, height); // image has no source!
        }

        // for embedded data we might have 20MB string length; so any trim() or so caises doubl;e copying of the buffers.
        // so, we have no way rather char-by-char processing until we sure it is safe to move back to human-readable form.
        // href = href.trim();

        int start = 0, end = href.length();
        while(start < end && Character.isWhitespace(href.charAt(start))) ++start;
        while(end-1 > start && Character.isWhitespace(href.charAt(end-1))) --end;
        if(start >= end) {
            Log.e("SVGKit", "href is empty");
            return new ImageRefSVG(x, y, width, height); // image has no source!
        }

        // link to sub-element itself, means the same as <use>
        if (href.charAt(start) == '#') { // href is URL and it is small
            href = href.trim();
            if (!recursion && !idleMode) {
                recursion = true;
                StringBuffer stb = defx.get(href.substring(1));
                if (stb != null) {
                    PaintData c = getContext(pp, parentContext(), gradientMap);
                    ctxs.push(c);

                    canvas.postTranslate(x.intValue(), y.intValue());
                    try {
                        StringReader in = new StringReader(stb.toString());
                        SAXParserFactory spf = SAXParserFactory.newInstance();
                        spf.setFeature("http://xml.org/sax/features/validation", false);
                        //	spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false); crush on android
                        SAXParser sp = spf.newSAXParser();
                        XMLReader xr = sp.getXMLReader();
                        xr.setContentHandler(this);
                        xr.parse(new InputSource(in));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    canvas.postTranslate(-x.intValue(), -y.intValue());
                    ctxs.pop();
                }
                recursion = false;
            }
            return new ImageRefSVG(x, y, width, height, true, !noKeep);
        }
        else if(href.substring(start, start + 12).startsWith("data:image/")) {
            // xlink:href="data:image/png;base64,iVBORw0...UVORK5CYII=" and it might be dozen of megabytes
            int k = href.indexOf(',');
            byte[] src = href.getBytes();
            href = null;
            boolean tooMuchDataNeeded = src.length > 65536;

            if(tooMuchDataNeeded) {
                // let GC clean all buffers collated before
                System.gc();
                System.gc();
            }

            byte[] data = Base64.decode(src, k + 1, end - k - 1, Base64.DEFAULT);
            //Log.wtf("SVGKit", "decoded=" + (data == null ? "(nope)" : "" + data.length));

            src = null;
            if(tooMuchDataNeeded) {
                // let GC clean all buffers collated before
                System.gc();
                System.gc();
            }

            Bitmap bb = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            if(tooMuchDataNeeded) {
                // let GC clean all buffers collated before
                System.gc();
                System.gc();
            }

            bb = Bitmap.createScaledBitmap(bb, (int)width.floatValue(), (int)height.floatValue(), false);
            if(tooMuchDataNeeded) {
                // let GC clean all buffers collated before
                System.gc();
                System.gc();
            }

            return new ImageRefSVG(x, y, width, height, !noKeep, bb);
        }
        else { // it is external URL - let asset manager to handle it
            //Log.wtf("SVGKit", "ExtREF=" + href + " base=" + parser.baseHref);
            try {
                InputStream in = parser.mExternalSupport.getExternalURL(parser.baseHref, href);
                if (in == null)
                    return new ImageRefSVG(x, y, width, height);
                else {
                    Bitmap bb = BitmapFactory.decodeStream(in);
                    bb = Bitmap.createScaledBitmap(bb, (int)width.floatValue(), (int)height.floatValue(), false);
                    bb.prepareToDraw();
                    //Log.wtf("SVGKit", href + " => " + bb.getWidth() + "x" + bb.getHeight());
                    return new ImageRefSVG(x, y, width, height, !noKeep, bb);
                }
            }
            catch (Exception e) {
                Log.wtf("SVGKit", href, e);
                return new ImageRefSVG(x, y, width, height, true, !noKeep);
            }
        }
    }

    private RectangleSVG parseRect(Properties pp, boolean stroke) {
        Float x = pp.getFloatAttr("x");
        if (x == null) {
            x = 0f;
        }
        Float y = pp.getFloatAttr("y");
        if (y == null) {
            y = 0f;
        }
        Float rWidth = pp.getScalledFloatAttr("width", ww);
        Float rHeight = pp.getScalledFloatAttr("height", hh);

        Float rx = pp.getFloatAttr("rx");
        Float ry = pp.getFloatAttr("ry");

        RectangleSVG rectangleSVG = null;
        if (rx != null || ry != null) {
            if (rx==null){
                rx=ry;//by spec
            } else {
                ry=rx;//by spec
            }
            rectangleSVG = new RectangleSVG(
                    x ,//+ corr,
                    y ,//+ corr,
                    rWidth ,//- corr * 1.8f,
                    rHeight ,//- corr * 1.8f,
                    rx, /** 2 */// - corr * 1.8f,
                    ry);// /** 2 */ - corr * 1.8f);
        } else {
            rectangleSVG = new RectangleSVG(
                    x, //+ corr,
                    y,// + corr,
                    rWidth,// - corr * 1.8f,
                    rHeight);// - corr * 1.8f);
        }
        return rectangleSVG;
    }

}