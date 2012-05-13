/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

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
package de.blizzy.documentr.web.markdown.macro;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.web.markdown.HtmlSerializerContext;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext.Header;

class TableOfContentsMacro extends AbstractMacro {
	@Override
	public String getHtml() {
		HtmlSerializerContext context = getHtmlSerializerContext();
		List<Header> headers = context.getHeaders();
		if (!headers.isEmpty()) {
			StringBuilder buf = new StringBuilder();
			for (Header header : headers) {
				buf.append(StringUtils.repeat("    ", header.level - 1)) //$NON-NLS-1$
					.append("- [[#").append(header.text).append("]]\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			String markdown = buf.toString() + "\n"; //$NON-NLS-1$
			return context.markdownToHTML(markdown);
		}
		return StringUtils.EMPTY;
	}
}
