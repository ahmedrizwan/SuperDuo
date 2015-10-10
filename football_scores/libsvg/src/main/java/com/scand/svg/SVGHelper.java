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
package com.scand.svg;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Build;
import android.view.View;

import com.scand.svg.parser.SVGParser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
This is a main class operating with library; it encapsulates typical appliances, 
conversions and so on.

Few examples: SVG as resource

<pre>
mImageView.setImageBitmap(
	SVGHelper.useContext(this).open(R.raw.star).getBitmap());
</pre>
<br>
Bit more complicated: SVG in external file

<pre>
File file = new File(Environment.getExternalStorageDirectory() + "/svg/star.svg");
mImageView.setImageBitmap(
	SVGHelper.noContext().open(file).getBitmap());
</pre>
 <br>
And even more: SVG from string

<pre>
mImageView.setImageBitmap(
	SVGHelper.noContext()
		.open(
			"&lt;svg viewBox=\"0 0 100 100\">\n" +
    		"	&lt;path d=\"M50,3l12,36h38l-30,22l11,36l-31-21l-31,21l11-36l-30-22h38z\"\n" +
		    "		fill=\"#FF0\" stroke=\"#FC0\" stroke-width=\"2\"/>\n" +
    		"	&lt;/svg>\n")
    	.getBitmap());
</pre>
 <br>
Features:
<ul>
<li>Pretty small
<li>Pure Java
<li>Reliable and fast
<li>CSS support
<li>Gradients (including radial), transformations and paints
<li>Fonts, texts and text spans
<li>Drawing primitives, paths and polygons
<li>Strokes and fills, including alpha, dashes, joins and other 
<li>XLink support included
</ul>

All calls are being designed in functiomal style like streams APIL first you need to initiate 
process by creating context, then "wire" the source, (optionally) add hints how it should be processed,
and then specify the result: Bitmap / Drawable. The getXXX() is a terminal element of the chain and it 
actually initiates SVG fetching, parsing and rendering.
*/
public class SVGHelper {

    private static Context context;

    /**
	Just contexct containing all parameters to be appended. 
	*/
    public static class SVGLoadData {

        private Context mContext;
        private String mSVGData;
        private URL mURL;
        private File mFile;
        private Integer mResId;
        private Integer mRequestWidth = null;
        private Integer mRequestHeight = null;
        private Integer mBaseWidth = null;
        private Integer mBaseHeight = null;
        private boolean mCheckSVGSize = false;
        private float mScale=1f;
        private SVGParser mSVGParser;
        private boolean mKeepAspectRatio = true;
        private boolean mCropImage = false;
        private String baseHref;

        /** 
        	use SVGHelper.useContext(...) as primary option. This constructor 
        	is availabke for certain strange cases for optional initialization of the chain.
         @param context - Context to operate with resources later.
        */
        public SVGLoadData(Context context) {
            this();
            mContext = context;
        }

        /**
        	use SVGHelper.noContext() to build this correctly.
        */
        public SVGLoadData() {
            mSVGParser = new SVGParser();
        }

        /**
         * Redefines asset manager if it should be different from useContext() call.
         * @param assetManager
         * @return context
         */
        public SVGLoadData registerAssetManager(AssetManager assetManager) {
            this.mSVGParser.registerAssetManager(assetManager);
            return this;
        }

        /**
         * Specify base URL for relative images and fonts, if api cannot detect it automatically
         * @param s
         * @return
         */
        public SVGLoadData setBaseHRef(String s) {
            this.baseHref = s;
            return this;
        }

        /** 
        	Indicates the source of the SVG. No real open is performed.
        */
        public SVGLoadData open(String svgData) throws IOException {
            mSVGData = svgData;
            this.baseHref = null;
            return this;
        }

        /** 
        	Indicates the source of the SVG. No real open is performed.
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData open(URL url) throws IOException {
            mURL = url;
            baseHref = url.toString() + "/..";
            return this;
        }

        /** 
        	Indicates the source of the SVG. No real open is performed.
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData open(File file) throws FileNotFoundException {
            mFile = file;
            baseHref = file.getAbsolutePath().replace('\\', '/') + "/..";
            return this;
        }

        /** 
        	Indicates the source of the SVG. No real open is performed.
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData open(int resId) {
            mResId = resId;
            return this;
        }

        /** 
        	Indicates the final bitmap size. Usable for resizes.
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData setRequestBounds(int width, int height) {
            mRequestWidth = width;
            mRequestHeight = height;
            return this;
        }

        /** 
        	Indicates the source SVG viewport as (0, 0, width, height).
        	Might be necessary if your SVG have no any 
        	width / height / viewPort specifications in &lt;svg/> tag.
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData setBaseBounds(int width, int height) {
            mBaseWidth = width;
            mBaseHeight = height;
            return this;
        }

        /**
        	Opposite to setBaseBounds(); indicates the sizes should be 
        	covered from &lt;svg/> tag of the source. If no sizes available,
        	the exception will be thrown at the time of real processing (not now).
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData checkSVGSize() {
            mCheckSVGSize = true;
            return this;
        }

        /**
        	Opposite to setRequestBounds(); indicates the size should be computed by 
        	multiplication of the scale to base sizes (either specified in setBaseSizes() or
        	computed automatically by checkSVGSize() methods).
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData setScale(float scale){
            mScale = scale;
            return this;
        }

        /**
        	At the scaling time, keep aspect ratio (default behavior). If SVG has size 100 x 100 
        	and you requesting 50 x 20, the final bitmap will be 50 x 20 but image will be
        	scaled to 20 x 20.

        	If set to false, image will be scaled to 50 x 20 and proportions will be lost.
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData setKeepAspectRatio(boolean keepAspectRatio){
            mKeepAspectRatio = keepAspectRatio;
            return this;
        }

        /**
        	Opposite to setKeepAspectRatio(); if SVG has size 100 x 100 
        	and you requesting 50 x 20, the final bitmap will be 50 x 20 but image will be
        	scaled to 50 x 50, and then cropped to 50 x 20.
         @return SVGLoadData itself to apply further tuning as a chain.
        */
        public SVGLoadData setCropImage(boolean cropImage){
            mCropImage = cropImage;
            return this;
        }

        private PointF preparseSVG(InputStream svg) {
            mSVGParser.preparse(svg, 0, 0);
            return new PointF(mSVGParser.width, mSVGParser.height);
        }

        private InputStream startStream() throws IOException {
            InputStream inputStream = null;
            if (mURL != null) {
                inputStream = mURL.openStream();
            } else if (mFile != null) {
                inputStream = new FileInputStream(mFile);
            } else if (mResId!=null){
                if (mContext != null) {
                    inputStream = mContext.getResources().openRawResource(mResId);
                    baseHref = mContext.getResources().getResourceName(mResId) + "/..";
                } else {
                    throw new IllegalArgumentException("Context must not be null.");
                }
            } else if (mSVGData!=null){
                inputStream = new ByteArrayInputStream(mSVGData.getBytes());
            }
            if (inputStream==null){
                throw new IllegalArgumentException("SVG source unknown. Use open method ");
            }
            return inputStream;
        }

        /**
        	Parses SVG with all options from context, then renders it into Bitmap raster image.
        	Might cause IOException if any i/o errors will occur.
         @return Bitmap containing rendered SVG
        */
        public Bitmap getBitmap() throws IOException {

            InputStream inputStream;
                inputStream = startStream();
                if (mCheckSVGSize) {
                    PointF page;
                    page = preparseSVG(inputStream);
                    inputStream.close();
                    inputStream = startStream();
                    mBaseWidth=(int)page.x;
                    mBaseHeight=(int)page.y;
                }
                SVG svg;
                svg = mSVGParser.parse(inputStream, (mBaseWidth != null ? mBaseWidth : 0), (mBaseHeight != null ? mBaseHeight : 0), (mRequestWidth != null ? mRequestWidth : 0), (mRequestHeight != null ? mRequestHeight : 0),mScale, mKeepAspectRatio, mCropImage, baseHref);
                return svg.getBitmap();
        }

        /**
        	Parses SVG with all options from context, then renders it into BitmapDrawable.
        	Might cause IOException if any i/o errors will occur.
         @return BitmapDrawable containing rendered SVG
        */
        public BitmapDrawable getBitmapDrawable() throws IOException {
            if (mContext != null) {
                return new BitmapDrawable(mContext.getResources(),getBitmap());
            } else {
                throw new IllegalArgumentException("Context must not be null.");
            }
        }

        /**
        	Parses SVG with all options from context, then renders it into Picture.
        	Might cause IOException if any i/o errors will occur.
         @return Picture containing rendered SVG
        */
        public Picture getPicture() throws IOException {

            InputStream inputStream;
            inputStream = startStream();
            if (mCheckSVGSize) {
                PointF page;
                page = preparseSVG(inputStream);
                inputStream.close();
                inputStream = startStream();
                mBaseWidth=(int)page.x;
                mBaseHeight=(int)page.y;
            }

            Picture picture = new Picture();
            int width=mRequestWidth != null ? mRequestWidth : 0;
            int height=mRequestHeight != null ? mRequestHeight : 0;
            Canvas canvas=picture.beginRecording(width, height);
            mSVGParser.render(inputStream,0,0,(mBaseWidth != null ? mBaseWidth : 0), (mBaseHeight != null ? mBaseHeight : 0),width, height,mScale, mKeepAspectRatio, mCropImage, baseHref, canvas);
            picture.endRecording();
            return picture;
        }

        /**
        	Parses SVG with all options from context, then renders it into PictureDrawable.
        	Might cause IOException if any i/o errors will occur.
         @return PictureDrawable containing rendered SVG
        */
        public PictureDrawable getPictureDrawable() throws IOException {
            return new PictureDrawable(getPicture());
        }

        /**
        	Parses SVG with all options from context, then renders it into Drawable, and
        	sets it as background of the button or view. setRequestBounds() are not necessary
        	to call as it will be added into chain automatically, with real sizes of the view.
        	
        	Might cause IOException if any i/o errors will occur.

         @param view - indicates the visual element with background assigned to.
        */
        public void bitmapAsBackground(View view) throws IOException {
            int width = view.getWidth();
            int height = view.getHeight();
            setRequestBounds(width,height);
            Drawable drawable= getBitmapDrawable();
            if (Build.VERSION.SDK_INT < 16) {
                view.setBackgroundDrawable(drawable);
            } else {
                view.setBackground(drawable);
            }
        }

        /**
        	Parses SVG with all options from context, then renders it into Drawable, and
        	sets it as background of the button or view. setRequestBounds() are not necessary
        	to call as it will be added into chain automatically, with real sizes of the view.
        	Handles all asynchronous issues if at the time of call the sizes are not computed.

        	Might cause IOException if any i/o errors will occur.
         @param view - View where SVG will be attached as background.
        */
        public void pictureAsBackground(final View view) throws IOException {
            final int width = view.getWidth();
            final int height = view.getHeight();
            if (width==0 && height==0){
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final int width = view.getWidth();
                            final int height = view.getHeight();
                            createAndApplyPicture(width, height, view);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                createAndApplyPicture(width, height, view);
            }
        }

        private void createAndApplyPicture(int width, int height, View view) throws IOException {
            setRequestBounds(width,height);
            Drawable drawable= getPictureDrawable();
            if (view.isHardwareAccelerated()){
                view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            if (Build.VERSION.SDK_INT < 16) {
                view.setBackgroundDrawable(drawable);
            } else {
                view.setBackground(drawable);
            }
        }

    }

    /**
    	Right way to initiate chain of the SVG processing. Useful if you have Context at this time.
    	Required if you want to use getBitmapDrawable() or specify source as resource.
        @param context - Context to operate with resources later.
        @return SVGLoadData itself to apply further tuning as a chain.
    */
    public static SVGLoadData useContext(Context context){
        SVGHelper.context = context;
        SVGLoadData ctx = new SVGLoadData(context);
        ctx.registerAssetManager(context.getAssets());
        return ctx;
    }

    /**
    	Right way to initiate chain of the SVG processing for case you have no context.
    	The getBitmapDrawable() or resource as source will cause IllegalArgumentException.
     @return SVGLoadData itself to apply further tuning as a chain.
    */
    public static SVGLoadData noContext(){
        return new SVGLoadData();
    }
}
