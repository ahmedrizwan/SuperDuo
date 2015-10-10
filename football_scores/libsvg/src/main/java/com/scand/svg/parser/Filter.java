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

import android.graphics.RectF;
import android.util.Log;

import com.scand.svg.parser.paintutil.FilterImpl;

import java.util.ArrayList;
import java.util.HashMap;

public class Filter {
	public ArrayList<FilterOp> op = new ArrayList<>();
    public String id;
    public String filterUnits = null;

    public Filter(){
    }

    public static String[] knownFe = {
            "feBlend", "feColorMatrix", "feComponentTransfer", "feComposite", "feConvolveMatrix",
            "feDiffuseLighting", "feDisplacementMap", "feDistantLight", "feFlood",
            "feFuncA", "feFuncR", "feFuncG", "feFuncB",
            "feGaussianBlur", "feImage", "feMerge", "feMergeNode", "fefeMorphology",
            "feOffset", "fePointLight", "feSpecularLighting", "feSpotLight",
            "feTile", "feTurbulence"
    };

    public static boolean isFilterOp(String localNode) {
        for(int k=0; k< knownFe.length; ++k)
            if(knownFe[k].equals(localNode)) {
                return true;
            }
        return false;
    }

    public static FilterImpl buildFilterImpl(FilterOp fop) {
        String fopName = fop.filterOp.substring(0, 1).toUpperCase() + fop.filterOp.substring(1);
        String cz = "com.scand.svg.parser.paintutil." + fopName;
        try {
            Class cc = Class.forName(cz);
            return (FilterImpl)cc.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void optimize(FilterOp fop, HashMap<String, FilterOp> results, ArrayList<FilterOp> newOp) {
        if(newOp.contains(fop)) return;

        FilterOp inop = fop.in != null && fop.in.length() > 0 ? results.get(fop.in) : null;
        FilterOp inop2 = fop.in2 != null && fop.in2.length() > 0 ? results.get(fop.in2) : null;

        // current have unresolved dependency...
        if(inop != null)
            optimize(inop, results, newOp);
        if(inop2 != null)
            optimize(inop2, results, newOp);
        newOp.add(fop);
    }

    public void optimize() {
        // we need to order all filters according to in, in2 and result clauses to let them work in right order
        // by the way we also  need to compute the infiltrated area but it is TODO

        // this trick is necessary just because all filters are specular and it ain't works
        // as ins SVG specs.It just specifies canvas' options like color filters, without real image composing.

         ArrayList<FilterOp> newOp = new ArrayList<>();

        HashMap<String, FilterOp> results = new HashMap<>();
        for(FilterOp fa : op) {
            if(fa.result != null && fa.result.length() > 0)
                results.put(fa.result, fa);
        }

        for(int k = 0; k < op.size(); ++k) {
            FilterOp fop = op.get(k);
            optimize(fop, results, newOp);
        }

        this.op = newOp;

        Log.i("SVGKit", "Fop[" + this.id + "," + this.filterUnits + "]{");
        for(FilterOp f : op) {
            Log.i("SVGKit", "   " + f.filterOp + ", {" + f.in + "," + f.in2 + "} => " + f.result);
        }
        Log.i("SVGKit", "}Fop");
    }
}
