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

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.markdown.Header;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.Macro;

@Macro(name="toc", insertText="{{toc/}}")
public class TableOfContentsMacro extends AbstractMarkdownMacroRunnable {
	@Override
	String getMarkdown(IMacroContext macroContext) {
		HtmlSerializerContext context = macroContext.getHtmlSerializerContext();
		List<Header> headers = context.getHeaders();
		if (!headers.isEmpty()) {
			int smallestLevel = Integer.MAX_VALUE;
			for (Header header : headers) {
				int level = header.getLevel() - 1;
				if (level < smallestLevel) {
					smallestLevel = level;
				}
			}

			StringBuilder buf = new StringBuilder();
			for (Header header : headers) {
				buf.append(StringUtils.repeat("    ", header.getLevel() - 1 - smallestLevel)) //$NON-NLS-1$
					.append("- [[#").append(header.getText()).append("]]\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return buf.toString() + "\n"; //$NON-NLS-1$
		}
		return null;
	}
}
