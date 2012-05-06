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
