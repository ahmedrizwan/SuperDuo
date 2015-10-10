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
package com.scand.svg.css;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class CSSURL extends CSSValue {

	private String url;

	protected CSSURL() {
	}

	protected CSSURL(String url) {
		this.url = url;
	}

	public void serialize(PrintWriter out) {
		out.print("url(");
		out.print(getURI());
		out.print(")");
	}

	public String toString() {
		return "url(" + getURI() + ")";
	}

	public String getURI() {
		return url;
	}

	public int hashCode() {
		return getURI().hashCode() + 17;
	}

	public boolean equals(Object other) {
		if (other.getClass() == getClass()) {
			CSSURL o = (CSSURL) other;
			return o.getURI().equals(getURI());
		}
		return false;
	}

	public String getContentType() {
		return "application/octet-stream";
	}

	public InputStream getInputStream() throws IOException {
		throw new RuntimeException("not implemented");
	}
	
	public byte[] getData() throws IOException {
		InputStream in = getInputStream();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		int len;
		while( (len = in.read(buf)) > 0 ) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
		return out.toByteArray();
	}
}
