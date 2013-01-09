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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pegdown.LinkRenderer.Attribute;
import org.pegdown.LinkRenderer.Rendering;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.WikiLinkNode;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.markdown.DocumentrLinkRenderer;
import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.util.Util;

public class DocumentrLinkRendererTest extends AbstractDocumentrTest {
	@Mock
	private HtmlSerializerContext context;
	@InjectMocks
	private DocumentrLinkRenderer renderer;

	@Test
	public void renderWikiLink() {
		WikiLinkNode node = new WikiLinkNode("foo"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("foo", rendering.text); //$NON-NLS-1$
	}

	@Test
	public void renderWikiLinkWithNoFollow() {
		WikiLinkNode node = new WikiLinkNode("foo | nofollow"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("foo", rendering.text); //$NON-NLS-1$
		assertTrue(rendering.attributes.contains(Attribute.NO_FOLLOW));
	}

	@Test
	public void renderWikiLinkWithText() {
		WikiLinkNode node = new WikiLinkNode("foo link text"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("link text", rendering.text); //$NON-NLS-1$
	}

	@Test
	public void renderWikiLinkWithTextAndNoFollow() {
		WikiLinkNode node = new WikiLinkNode("foo link text | nofollow"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("link text", rendering.text); //$NON-NLS-1$
		assertTrue(rendering.attributes.contains(Attribute.NO_FOLLOW));
	}

	@Test
	public void renderWikiLinkWithAnchor() {
		String headline = "A Headline"; //$NON-NLS-1$
		WikiLinkNode node = new WikiLinkNode("#" + headline); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("#" + Util.simplifyForUrl(headline), rendering.href); //$NON-NLS-1$
		assertEquals(headline, rendering.text);
	}

	@Test
	public void renderWikiLinkToAttachment() {
		when(context.getAttachmentUri("foo")).thenReturn("fooAttachment"); //$NON-NLS-1$ //$NON-NLS-2$
		WikiLinkNode node = new WikiLinkNode("=foo"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("fooAttachment", rendering.href); //$NON-NLS-1$
		assertEquals("foo", rendering.text); //$NON-NLS-1$
	}

	@Test
	public void renderWikiLinkToAttachmentWithNoFollow() {
		when(context.getAttachmentUri("foo")).thenReturn("fooAttachment"); //$NON-NLS-1$ //$NON-NLS-2$
		WikiLinkNode node = new WikiLinkNode("=foo | nofollow"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("fooAttachment", rendering.href); //$NON-NLS-1$
		assertEquals("foo", rendering.text); //$NON-NLS-1$
		assertTrue(rendering.attributes.contains(Attribute.NO_FOLLOW));
	}

	@Test
	public void renderWikiLinkToAttachmentWithText() {
		when(context.getAttachmentUri("foo")).thenReturn("fooAttachment"); //$NON-NLS-1$ //$NON-NLS-2$
		WikiLinkNode node = new WikiLinkNode("=foo link text"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("fooAttachment", rendering.href); //$NON-NLS-1$
		assertEquals("link text", rendering.text); //$NON-NLS-1$
	}

	@Test
	public void renderWikiLinkToAttachmentWithTextAndNoFollow() {
		when(context.getAttachmentUri("foo")).thenReturn("fooAttachment"); //$NON-NLS-1$ //$NON-NLS-2$
		WikiLinkNode node = new WikiLinkNode("=foo link text | nofollow"); //$NON-NLS-1$
		Rendering rendering = renderer.render(node);
		assertEquals("fooAttachment", rendering.href); //$NON-NLS-1$
		assertEquals("link text", rendering.text); //$NON-NLS-1$
		assertTrue(rendering.attributes.contains(Attribute.NO_FOLLOW));
	}

	@Test
	public void renderExpLinkWithAnchor() {
		String headline = "A Headline"; //$NON-NLS-1$
		ExpLinkNode node = new ExpLinkNode(null, "#" + headline, null); //$NON-NLS-1$
		Rendering rendering = renderer.render(node, headline);
		assertEquals("#" + Util.simplifyForUrl(headline), rendering.href); //$NON-NLS-1$
		assertEquals(headline, rendering.text);
	}

	@Test
	public void renderExpLinkToPage() {
		when(context.getPageUri("foo")).thenReturn("fooPage"); //$NON-NLS-1$ //$NON-NLS-2$
		ExpLinkNode node = new ExpLinkNode(null, ":foo", null); //$NON-NLS-1$
		Rendering rendering = renderer.render(node, "text"); //$NON-NLS-1$
		assertEquals("fooPage", rendering.href); //$NON-NLS-1$
		assertEquals("text", rendering.text); //$NON-NLS-1$
	}
}
