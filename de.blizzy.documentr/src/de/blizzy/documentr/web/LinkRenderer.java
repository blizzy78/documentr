package de.blizzy.documentr.web;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ast.WikiLinkNode;

/*
 * allows the following Wiki-style links:
 * 
 * [[URI]]
 * [[URI link text]]
 */
class LinkRenderer extends org.pegdown.LinkRenderer {
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
