package com.scand.svg.parser.paintutil;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;

import com.scand.svg.parser.FilterOp;
import com.scand.svg.parser.support.GraphicsSVG;

public class FeBlend extends FilterImpl {
    public FeBlend() {
    }

    @Override
    public void handle(FilterOp fop, GraphicsSVG canvas, boolean stroke, RectF bounds)
    {
        // '', 'normal', 'multiply', 'screen', 'darken', 'lighten'
        if(fop.mode == null || fop.mode.length() == 0) fop.mode = "normal";

        if(fop.mode.equals("normal"))
            canvas.setComposite(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        else if(fop.mode.equals("multiply"))
            canvas.setComposite(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        else if(fop.mode.equals("screen"))
            canvas.setComposite(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        else if(fop.mode.equals("lighten"))
            canvas.setComposite(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
        else if(fop.mode.equals("darken"))
            canvas.setComposite(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        else
            Log.w("SVGkit", "feBlend: unknown mode `" + fop.mode + "`");
    }
}
