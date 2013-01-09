/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012-2013 Maik Schreiber

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
package de.blizzy.documentr.page;

import com.google.common.base.Charsets;

public class PageTextData extends PageData {
	static final String CONTENT_TYPE = "text/plain"; //$NON-NLS-1$

	public PageTextData(String text) {
		super(text.getBytes(Charsets.UTF_8), CONTENT_TYPE);
	}

	static PageTextData fromBytes(byte[] data) {
		return new PageTextData(new String(data, Charsets.UTF_8));
	}

	public String getText() {
		return new String(getData(), Charsets.UTF_8);
	}
}
