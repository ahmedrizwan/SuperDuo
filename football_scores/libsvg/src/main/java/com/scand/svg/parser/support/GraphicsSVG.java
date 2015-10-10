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
package com.scand.svg.parser.support;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Xfermode;

import com.scand.svg.parser.ExternalSupport;

public class GraphicsSVG {

    public static final String MATRIX ="matrix";
    public static final String COLOR="color";

    private Canvas mCanvas;
    private Paint mPaintStroke;
    private Paint mPaintFill;
    private Paint mPaintText;//TODO check. Possibly useless
    private Rect mTextSize;
    private String[] mFontNames;
    private int storedPaintFillColor;
    private int storedPaintStrokeColor;
    private ColorSVG mGradientOpacity;
    private Shader mGradient;
    private Xfermode composite;

    public GraphicsSVG(Canvas canvas){
        mCanvas = canvas;
        mPaintStroke = new Paint();
        mPaintStroke.setAntiAlias(true);
        mPaintStroke.setStyle(Paint.Style.STROKE);
        mPaintFill = new Paint();
        mPaintFill.setAntiAlias(true);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintText = new Paint();
        mPaintText.setAntiAlias(true);
        mPaintText.setStyle(Paint.Style.FILL_AND_STROKE);
        composite = null;
    }

    public Xfermode getComposite() { return composite; }
    public void setComposite(Xfermode x) { composite = x; }

    public GraphicsSVG(Bitmap bitmap){
        this(new Canvas(bitmap));
    }


    public void postTranslate(int x, int y){
        mCanvas.translate(x, y);
    }

    public void setFillColor(int color){
       // Log.d(COLOR, " "+android.graphics.Color.alpha(color)+" "+android.graphics.Color.red(color)+" "+android.graphics.Color.green(color)+" "+android.graphics.Color.blue(color)+" "+ color);
        mPaintFill.setColor(color);
    }

    public void setStrokeColor(int color){
      //  Log.d(COLOR, " "+android.graphics.Color.alpha(color)+" "+android.graphics.Color.red(color)+" "+android.graphics.Color.green(color)+" "+android.graphics.Color.blue(color)+" "+ color);
        mPaintStroke.setColor(color);
    }

    public void setTextColor(int color){
      //  Log.d(COLOR, " "+android.graphics.Color.alpha(color)+" "+android.graphics.Color.red(color)+" "+android.graphics.Color.green(color)+" "+android.graphics.Color.blue(color)+" "+ color);
        mPaintText.setColor(color);
    }

    public void transform(Matrix matrix, ClipPath clipPath){
        mCanvas.save();
        if (matrix!=null)
        mCanvas.concat(matrix);
        if (clipPath!=null) {
            mCanvas.clipPath(clipPath.getPath(), Region.Op.INTERSECT);
       //     Log.d("CLIP", "clip");
        }
    }

    public void save(){
        mCanvas.save();
    }

    public void restore(){
        mCanvas.restore();
    }

    public void setTransform(Matrix transform){
        mCanvas.setMatrix(transform);
        mCanvas.save();
    }

    public void setStroke(BasicStroke stroke){
        mPaintStroke.setStrokeWidth(stroke.mLineWidth);
        mPaintStroke.setStrokeCap(stroke.mCapStyle);
        mPaintStroke.setStrokeJoin(stroke.mJoinStyle);
        if (stroke.mDashArray!=null) {
            mPaintStroke.setPathEffect(new DashPathEffect(stroke.mDashArray, stroke.mDashOffset));
        } else {
            mPaintStroke.setPathEffect(null);
        }
    }

    public void fillRect(int x1, int y1, int x2, int y2){
        Paint paint = new Paint();
        //paint.setColor(android.graphics.Color.WHITE);
        paint.setColor(Color.TRANSPARENT); //transparent bg is more useful
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        mCanvas.drawRect(x1,y1,x2,y2,paint);
    }

    public int stringWidth(String text){
        float[] widths = new float[text.length()];
        mPaintText.getTextWidths(text, widths);
        float result=0;
        for (float width:widths){
            result+=width;
        }
        return Math.round(result);
    }

    public void draw(GraphElement graphObj){
        setGradientFill(false);
        Xfermode oldXfer = mPaintStroke.getXfermode();
        if(composite != null)
            mPaintStroke.setXfermode(composite);
        graphObj.draw(mCanvas, mPaintStroke);
        if(composite != null)
            mPaintStroke.setXfermode(oldXfer);
        clearGradient(false);
    }

    public void fill(GraphElement graphObj){
        setGradientFill(true);
        Xfermode oldXfer = mPaintFill.getXfermode();
        if(composite != null)
            mPaintFill.setXfermode(composite);
        graphObj.draw(mCanvas, mPaintFill);
        if(composite != null)
            mPaintFill.setXfermode(oldXfer);
        clearGradient(true);
    }

    public void drawString(String str, float fx, float fy){
        mCanvas.drawText(str,fx,fy,mPaintText);
    }

    public void setFont(String[] name, int style, int size,ExternalSupport externalSupport){
        mFontNames = name;
        Typeface typeface = createNewFont(name, style, externalSupport);
        mPaintText.setTextSize(size);
        mPaintText.setTypeface(typeface);
    }

    public void deriveFont(){

    }

    public void deriveFont(int style, ExternalSupport externalSupport){
        Typeface typeface= createNewFont(mFontNames, style, externalSupport);
        mPaintText.setTypeface(typeface);
    }

    public void deriveFont(float size){
        mPaintText.setTextSize(size);
    }

    public void deriveFont( int style, int size, ExternalSupport externalSupport){
        Typeface typeface= createNewFont(mFontNames, style,externalSupport);
        mPaintText.setTextSize(size);
        mPaintText.setTypeface(typeface);
    }

    public int getFontSize(){
        return Math.round(mPaintText.getTextSize());
    }

    public int getFontStyle(){
        return mPaintText.getTypeface().getStyle();
    }

    private Typeface getExternalFont(String fontName, int fontStyle, ExternalSupport externalSupport)
    {
        if (externalSupport!=null){
            return externalSupport.getExternalFont(fontName,fontStyle);
        }else{
            return null;
        }
    }

    public static String dequote(String token) {
        if (token == null) {
            return "";
        }

        String t = token.trim();

        if ((t.startsWith("\"") && t.endsWith("\"")) ||
                (t.startsWith("'") && t.endsWith("'")) ) {
            return t.substring(1, t.length() - 1).trim();
        } else {
            return t;
        }
    }

    private Typeface createNewFont(String[] names, int style, ExternalSupport externalSupport){
        Typeface typeface = null;
        if (names!=null) {
            for (String name : names) {
                name = dequote(name);
                typeface = checkAndroidFont(name, style);
                if (typeface == null && names[0].equals(name)) { // name[0] might have an URL
                    typeface = getExternalFont(name, style, externalSupport);
                }
                if(typeface != null) break;
            }
        }

        if (typeface == null) {
            typeface = Typeface.create(Typeface.DEFAULT, style);
        }
        return typeface;
    }

    private Typeface checkAndroidFont(String fontName, int fontStyle)
    {
        Typeface font = null;

        if (fontName.equals("serif")) {
            font = Typeface.create(Typeface.SERIF, fontStyle);
        } else if (fontName.equals("sans-serif")) {
            font = Typeface.create(Typeface.SANS_SERIF, fontStyle);
        } else if (fontName.equals("monospace")) {
            font = Typeface.create(Typeface.MONOSPACE, fontStyle);
        }
        return font;
    }

    private int[] colorsProcess(ColorSVG[] colorsjava){
        int[] colors = new int[colorsjava.length];
        int i=0;
        for (ColorSVG color:colorsjava){
            colors[i]=color.mValue;
            i++;
        }
        return colors;
    }


    public void setGradient(RadialGradientPaint gradient, ColorSVG alphaColor){
        int[] colors= colorsProcess(gradient.colors);
        RadialGradient radialGradient= new RadialGradient(gradient.x,gradient.y,gradient.rad,colors,gradient.positions,gradient.tileMode);
        mGradient=radialGradient;
        mGradient.setLocalMatrix(gradient.matrix);
        mGradientOpacity = alphaColor;
    }

    public void setGradient(LinearGradientPaint gradient, ColorSVG alphaColor){
        int[] colors= colorsProcess(gradient.colors);
        LinearGradient linearGradient = new LinearGradient(gradient.x1,gradient.y1,gradient.x2,gradient.y2,colors,gradient.positions,gradient.mTileMode);
        mGradient=linearGradient;
        mGradient.setLocalMatrix(gradient.matrix);
        mGradientOpacity = alphaColor;
    }

    private void setGradientFill(boolean isFill){
        Paint paint = (isFill?mPaintFill:mPaintStroke);
        if (mGradient!=null){
            if (isFill) {
                storedPaintFillColor = paint.getColor();
            } else {
                storedPaintStrokeColor = paint.getColor();
            }
            paint.setColor(mGradientOpacity.mValue);
            paint.setShader(mGradient);
        }
    }

    private void clearGradient(boolean isFill){
        Paint paint = (isFill?mPaintFill:mPaintStroke);
        paint.setColor(isFill?storedPaintFillColor:storedPaintStrokeColor);
        paint.setShader(null);
        mGradient=null;
        mGradientOpacity=null;
    }
}
