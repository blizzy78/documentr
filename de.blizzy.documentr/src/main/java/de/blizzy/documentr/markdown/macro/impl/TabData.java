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
package de.blizzy.documentr.markdown.macro.impl;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.util.Util;

class TabData implements Serializable {
	private static final long serialVersionUID = -1447181245100718662L;

	@Getter(AccessLevel.PACKAGE)
	private String title;
	@Getter(AccessLevel.PACKAGE)
	private String contents;

	TabData(String title, String contents) {
		this.title = title;
		this.contents = contents;
	}

	static TabData deserialize(String s) {
		return Util.deserialize(s, TabData.class);
	}

	String serialize() {
		return Util.serialize(this);
	}

	@Override
	public String toString() {
		return TabData.class.getSimpleName() + "[title=" + title + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	String renderTab(String id, boolean active) {
		return "<li" + (active ? " class=\"active\"" : StringUtils.EMPTY) + ">" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"<a href=\"#" + id + "\" data-toggle=\"tab\">" + //$NON-NLS-1$ //$NON-NLS-2$
				StringEscapeUtils.escapeHtml4(title) +
				"</a>" + //$NON-NLS-1$
				"</li>"; //$NON-NLS-1$
	}

	String renderContents(String id, boolean active) {
		return "<div id=\"" + id + "\" class=\"tab-pane" + (active ? " active" : StringUtils.EMPTY) + "\">" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				contents +
				"</div>"; //$NON-NLS-1$
	}
}
