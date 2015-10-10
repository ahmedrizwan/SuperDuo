package com.scand.svg.parser.paintutil;

import android.graphics.RectF;

import com.scand.svg.parser.FilterOp;
import com.scand.svg.parser.support.GraphicsSVG;

/**
 * Basic class for filter implementations.
 */
public abstract class FilterImpl {
    public FilterImpl() {
    }

    public abstract void handle(FilterOp fop, GraphicsSVG canvas, boolean stroke, RectF bounds);
}
