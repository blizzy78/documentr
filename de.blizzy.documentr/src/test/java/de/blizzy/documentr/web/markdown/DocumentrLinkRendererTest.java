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

import static org.junit.Assert.*;

import org.junit.Test;
import org.pegdown.LinkRenderer.Attribute;
import org.pegdown.LinkRenderer.Rendering;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.WikiLinkNode;

import de.blizzy.documentr.Util;

public class DocumentrLinkRendererTest {
	@Test
	public void renderWikiLink() {
		WikiLinkNode node = new WikiLinkNode("foo"); //$NON-NLS-1$
		Rendering rendering = new DocumentrLinkRenderer().render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("foo", rendering.text); //$NON-NLS-1$
	}
	
	@Test
	public void renderWikiLinkWithNoFollow() {
		WikiLinkNode node = new WikiLinkNode("foo | nofollow"); //$NON-NLS-1$
		Rendering rendering = new DocumentrLinkRenderer().render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("foo", rendering.text); //$NON-NLS-1$
		assertTrue(rendering.attributes.contains(Attribute.NO_FOLLOW));
	}
	
	@Test
	public void renderWikiLinkWithText() {
		WikiLinkNode node = new WikiLinkNode("foo link text"); //$NON-NLS-1$
		Rendering rendering = new DocumentrLinkRenderer().render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("link text", rendering.text); //$NON-NLS-1$
	}
	
	@Test
	public void renderWikiLinkWithTextAndNoFollow() {
		WikiLinkNode node = new WikiLinkNode("foo link text | nofollow"); //$NON-NLS-1$
		Rendering rendering = new DocumentrLinkRenderer().render(node);
		assertEquals("foo", rendering.href); //$NON-NLS-1$
		assertEquals("link text", rendering.text); //$NON-NLS-1$
		assertTrue(rendering.attributes.contains(Attribute.NO_FOLLOW));
	}
	
	@Test
	public void renderWikiLinkWithAnchor() {
		String headline = "A Headline"; //$NON-NLS-1$
		WikiLinkNode node = new WikiLinkNode("#" + headline); //$NON-NLS-1$
		Rendering rendering = new DocumentrLinkRenderer().render(node);
		assertEquals("#" + Util.simplifyForURL(headline), rendering.href); //$NON-NLS-1$
		assertEquals(headline, rendering.text);
	}
	
	@Test
	public void renderExpLinkNodeWithAnchor() {
		String headline = "A Headline"; //$NON-NLS-1$
		ExpLinkNode node = new ExpLinkNode(null, "#" + headline, null); //$NON-NLS-1$
		Rendering rendering = new DocumentrLinkRenderer().render(node, headline);
		assertEquals("#" + Util.simplifyForURL(headline), rendering.href); //$NON-NLS-1$
		assertEquals(headline, rendering.text);
	}
}
