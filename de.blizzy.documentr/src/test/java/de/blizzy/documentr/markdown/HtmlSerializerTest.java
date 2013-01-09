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

import static de.blizzy.documentr.TestUtil.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;

import com.google.common.collect.Lists;

import de.blizzy.documentr.AbstractDocumentrTest;

public class HtmlSerializerTest extends AbstractDocumentrTest {
	@Mock
	private HtmlSerializerContext context;
	@InjectMocks
	private HtmlSerializer htmlSerializer;

	@Test
	public void visitVerbatimNode() {
		String text = "x\ny\nz"; //$NON-NLS-1$
		VerbatimNode node = new VerbatimNode(text);
		String html = htmlSerializer.toHtml(root(node));
		assertEquals("<div class=\"code-view-wrapper\"><!--__NOTRIM__--><div class=\"code-view\">" + //$NON-NLS-1$
				text + "</div><!--__/NOTRIM__--></div>\n", //$NON-NLS-1$
				removeTextRange(html));
	}

	@Test
	public void visitVerbatimNodeWithType() {
		String text = "x\ny\nz"; //$NON-NLS-1$
		VerbatimNode node = new VerbatimNodeWithType(text, "xml"); //$NON-NLS-1$
		String html = htmlSerializer.toHtml(root(node));
		assertEquals("<div class=\"code-view-wrapper\"><!--__NOTRIM__--><div class=\"code-view\" data-type=\"xml\">" + //$NON-NLS-1$
				text + "</div><!--__/NOTRIM__--></div>\n", //$NON-NLS-1$
				removeTextRange(html));
	}

	@Test
	public void visitVerbatimNodeWithTypeAndTitle() {
		String text = "x\ny\nz"; //$NON-NLS-1$
		VerbatimNode node = new VerbatimNodeWithType(text, "xml", "title"); //$NON-NLS-1$ //$NON-NLS-2$
		String html = htmlSerializer.toHtml(root(node));
		assertEquals("<div class=\"code-view-title\">title</div>" + //$NON-NLS-1$
				"<div class=\"code-view-wrapper\"><!--__NOTRIM__--><div class=\"code-view\" data-type=\"xml\">" + //$NON-NLS-1$
				text + "</div><!--__/NOTRIM__--></div>\n", //$NON-NLS-1$
				removeTextRange(html));
	}

	@Test
	public void printTable() {
		TableNode node = new TableNode();
		String html = htmlSerializer.toHtml(root(node));
		assertEquals("<table class=\"table-documentr table-bordered table-striped table-condensed\"></table>", //$NON-NLS-1$
				removeTextRange(html));
	}

	@Test
	public void printImageTag() {
		when(context.getAttachmentUri("url")).thenReturn("/foo.png"); //$NON-NLS-1$ //$NON-NLS-2$

		RefImageNode node = new RefImageNode(
				new SuperNode(new TextNode("foo.png")), null, new TextNode(StringUtils.EMPTY)); //$NON-NLS-1$
		RootNode rootNode = root(node);
		ReferenceNode refNode = new ReferenceNode(new TextNode("foo.png")); //$NON-NLS-1$
		refNode.setUrl("url"); //$NON-NLS-1$
		rootNode.setReferences(Lists.newArrayList(refNode));
		String html = htmlSerializer.toHtml(rootNode);
		assertEquals("<img src=\"/foo.png\"/>", html); //$NON-NLS-1$
	}

	@Test
	public void printImageTagWithAltText() {
		when(context.getAttachmentUri("url")).thenReturn("/foo.png"); //$NON-NLS-1$ //$NON-NLS-2$

		RefImageNode node = new RefImageNode(
				new SuperNode(new TextNode("foo.png")), null, new TextNode("alttext")); //$NON-NLS-1$ //$NON-NLS-2$
		RootNode rootNode = root(node);
		ReferenceNode refNode = new ReferenceNode(new TextNode("foo.png")); //$NON-NLS-1$
		refNode.setUrl("url"); //$NON-NLS-1$
		rootNode.setReferences(Lists.newArrayList(refNode));
		String html = htmlSerializer.toHtml(rootNode);
		assertEquals("<img src=\"/foo.png\" alt=\"alttext\"/>", html); //$NON-NLS-1$
	}

	@Test
	public void printImageTagAsThumbnail() {
		when(context.getAttachmentUri("url")).thenReturn("/foo.png"); //$NON-NLS-1$ //$NON-NLS-2$

		RefImageNode node = new RefImageNode(
				new SuperNode(new TextNode("foo.png | thumbnail")), null, new TextNode("alttext")); //$NON-NLS-1$ //$NON-NLS-2$
		RootNode rootNode = root(node);
		ReferenceNode refNode = new ReferenceNode(new TextNode("foo.png | thumbnail")); //$NON-NLS-1$
		refNode.setUrl("url | thumbnail"); //$NON-NLS-1$
		rootNode.setReferences(Lists.newArrayList(refNode));
		String html = htmlSerializer.toHtml(rootNode);
		assertEquals("<ul class=\"thumbnails\"><li class=\"span3\"><a class=\"thumbnail\" " + //$NON-NLS-1$
				"href=\"/foo.png\"><img src=\"/foo.png\" alt=\"alttext\" data-lightbox=\"lightbox\" " + //$NON-NLS-1$
				"width=\"260\"/></a></li></ul>", html); //$NON-NLS-1$
	}

	@Test
	public void printImageTagWithTitle() {
		when(context.getAttachmentUri("url")).thenReturn("/foo.png"); //$NON-NLS-1$ //$NON-NLS-2$

		ExpImageNode node = new ExpImageNode("title", "url", new TextNode("alttext")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String html = htmlSerializer.toHtml(root(node));
		assertEquals("<img src=\"/foo.png\" alt=\"alttext\" rel=\"tooltip\" data-title=\"title\"/>", html); //$NON-NLS-1$
	}

	@Test
	public void visitHeaderNode() {
		HeaderNode node = new HeaderNode(2, new TextNode("A Headline")); //$NON-NLS-1$
		String html = htmlSerializer.toHtml(root(node));
		assertEquals("<a name=\"a-headline\"></a><h3>A Headline</h3>", removeTextRange(html)); //$NON-NLS-1$

		verify(context).addHeader("A Headline", 2); //$NON-NLS-1$
	}

	@Test
	public void visitMacroNode() {
		MacroInvocation invocation = new MacroInvocation("macro", "params"); //$NON-NLS-1$ //$NON-NLS-2$
		when(context.addMacroInvocation("macro", "params")).thenReturn(invocation); //$NON-NLS-1$ //$NON-NLS-2$

		MacroNode node = new MacroNode("macro", "params"); //$NON-NLS-1$ //$NON-NLS-2$
		String html = htmlSerializer.toHtml(root(node));
		assertEquals(invocation.getStartMarker() + invocation.getEndMarker(), html);
	}

	private RootNode root(Node child) {
		RootNode rootNode = new RootNode();
		rootNode.getChildren().add(child);
		return rootNode;
	}
}
