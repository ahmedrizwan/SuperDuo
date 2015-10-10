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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
	Handles fonts from assets.
*/
public class ExternalSupport {

    AssetManager mAssertManager;

    public ExternalSupport(AssetManager assetManager){
        mAssertManager=assetManager;
    }

    public Typeface getExternalFont(String fontRef, int fontStyle){
        // first case: it is asset
        try{
            return Typeface.createFromAsset(mAssertManager, fontRef);
        }
        catch (Exception e) {
        }

        // second case: file somewhere
        try {
            File f = new File(fontRef);
            if(!f.exists())
                f = new File(Environment.getDownloadCacheDirectory(), fontRef);
            if(!f.exists())
                f = new File(Environment.getDataDirectory(), fontRef);
            if(!f.exists())
                f = new File(Environment.getExternalStorageDirectory(), fontRef);
            if(!f.exists())
                f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fontRef);
            if(f.exists())
                return Typeface.createFromFile(f);
        }
        catch(Exception e2) {
        }

        try {
            File f = downloadToCache(fontRef);
            if(f!= null && f.exists())
                return Typeface.createFromFile(f);
        }
        catch(Exception e3) {
        }

        return Typeface.create(fontRef, fontStyle);
    }

    public File downloadToCache(String href) {
        try {
            URL u = new URL(href);
            String ref = u.getFile();
            if(ref.indexOf('/') >=0) ref = ref.substring(ref.lastIndexOf('/') + 1);
            File f = new File(Environment.getDownloadCacheDirectory(), ref);
            if(f.exists()) return f;

            HttpURLConnection urlConnection = (HttpURLConnection) u.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            FileOutputStream fileOutput = new FileOutputStream(f);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;

            byte[] buffer = new byte[65536];
            int bufferLength = 0; //used to store a temporary size of the buffer
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
            }
            fileOutput.close();
            inputStream.close();
            return f;

        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    private String canonify(String s) {
        for(;;) {
            int k = s.indexOf("/..");
            if(k < 0) break;
            int j = s.lastIndexOf('/', k - 1);
            if(j > 0)
                s = s.substring(0, j) + s.substring(k + 3);
            else
                s = s.substring(0, k) + s.substring(k + 3);
        }
        return s;
    }

    public InputStream getExternalURL(String base, String href){
        if(!href.startsWith("/") && href.indexOf(':') < 0) { // relative path
            if(base != null) {
                if(!base.endsWith("/")) base += "/";
                href = base + href;
            }
        }

        String goodRef = canonify(href);
        //Log.wtf("SVGKit", "--> [" + goodRef + "]");
        int k = goodRef.indexOf(':');
        if(k > 0 && !goodRef.startsWith("android.resource:")) {
            goodRef = goodRef.substring(k + 1);
        }
        if(goodRef.startsWith("raw/")) goodRef = goodRef.substring(4);

        try {
            return mAssertManager.open(goodRef);
        }
        catch (Exception e) {
            //Log.wtf("SVGKit", "--> [" + href + "]", e);
        }

        try {
            File f = new File(goodRef);
            if (f.exists())
                return new FileInputStream(f);
        }
        catch(Exception e2) {
        }

        try { // seems it is an external resource
            return new URL(href).openStream();
        }
        catch(Exception e3) {
        }

        return null;
    }
}
