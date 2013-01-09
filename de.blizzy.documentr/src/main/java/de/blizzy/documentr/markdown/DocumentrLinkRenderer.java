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
package de.blizzy.documentr.markdown;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.WikiLinkNode;
import org.springframework.util.Assert;

import de.blizzy.documentr.util.Util;

class DocumentrLinkRenderer extends LinkRenderer {
	private HtmlSerializerContext context;

	DocumentrLinkRenderer(HtmlSerializerContext context) {
		Assert.notNull(context);

		this.context = context;
	}

	/*
	 * allows the following Wiki-style links:
	 *
	 * [[URI]]
	 * [[URI | nofollow]]
	 * [[URI link text]]
	 * [[URI link text | nofollow]]
	 * [[=attachment]]
	 * [[=attachment | nofollow]]
	 * [[=attachment link text]]
	 * [[=attachment link text | nofollow]]
	 * [[#Headline]]
	 */
	@Override
	public Rendering render(WikiLinkNode node) {
		String text = node.getText();
		String uri;
		boolean noFollow = false;
		if (text.startsWith("#")) { //$NON-NLS-1$
			text = text.substring(1).trim();
			uri = "#" + Util.simplifyForUrl(text); //$NON-NLS-1$
		} else {
			uri = StringUtils.substringBefore(text, " ").trim(); //$NON-NLS-1$
			text = StringUtils.substringAfter(text, " "); //$NON-NLS-1$
			String params = StringUtils.substringAfter(text, "|").trim(); //$NON-NLS-1$
			text = StringUtils.substringBefore(text, "|"); //$NON-NLS-1$

			if (uri.startsWith("=")) { //$NON-NLS-1$
				if (StringUtils.isBlank(text)) {
					text = uri.substring(1);
				}

				uri = context.getAttachmentUri(uri.substring(1));
			} else {
				if (StringUtils.isBlank(text)) {
					text = uri;
				}
			}

			text = text.trim();

			if (params.equalsIgnoreCase("nofollow")) { //$NON-NLS-1$
				noFollow = true;
			}
		}
		Rendering rendering = new Rendering(uri, text);
		if (noFollow) {
			rendering.withAttribute(Attribute.NO_FOLLOW);
		}
		return rendering;
	}

	/*
	 * allows the following link:
	 *
	 * [text](:page)
	 * [text](#Headline)
	 */
	@Override
	public Rendering render(ExpLinkNode node, String text) {
		if (node.url.startsWith("#")) { //$NON-NLS-1$
			List<Node> children = node.getChildren();
			Node child = !children.isEmpty() ? children.get(0) : null;
			String url = "#" + Util.simplifyForUrl(node.url.substring(1)); //$NON-NLS-1$
			node = new ExpLinkNode(node.title, url, child);
		} else if (node.url.startsWith(":")) { //$NON-NLS-1$
			List<Node> children = node.getChildren();
			Node child = !children.isEmpty() ? children.get(0) : null;
			String url = context.getPageUri(node.url.substring(1));
			node = new ExpLinkNode(node.title, url, child);
		}
		return super.render(node, text);
	}
}
