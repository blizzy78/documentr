package de.blizzy.documentr.web;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pegdown.LinkRenderer.Rendering;
import org.pegdown.ast.WikiLinkNode;

public class LinkRendererTest {
	@Test
	public void renderWikiLink() {
		WikiLinkNode node = new WikiLinkNode("foo"); //$NON-NLS-1$
		Rendering rendering = new LinkRenderer().render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("foo", rendering.text); //$NON-NLS-1$
	}
	
	@Test
	public void renderWikiLinkWithText() {
		WikiLinkNode node = new WikiLinkNode("foo link text"); //$NON-NLS-1$
		Rendering rendering = new LinkRenderer().render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("link text", rendering.text); //$NON-NLS-1$
	}
}
