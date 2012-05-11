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
package de.blizzy.documentr.web.markdown;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ast.WikiLinkNode;

/*
 * allows the following Wiki-style links:
 * 
 * [[URI]]
 * [[URI link text]]
 */
class DocumentrLinkRenderer extends LinkRenderer {
	@Override
	public Rendering render(WikiLinkNode node) {
		String text = node.getText();
		String uri = StringUtils.substringBefore(text, " "); //$NON-NLS-1$
		text = StringUtils.substringAfter(text, " "); //$NON-NLS-1$
		if (StringUtils.isBlank(text)) {
			text = uri;
		}
		text = text.trim();
		
		return new Rendering(uri, text);
	}
}
