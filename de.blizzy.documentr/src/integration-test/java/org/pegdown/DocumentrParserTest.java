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
package org.pegdown;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.parboiled.Parboiled;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;

import de.blizzy.documentr.markdown.MacroNode;
import de.blizzy.documentr.markdown.PageHeaderNode;
import de.blizzy.documentr.markdown.VerbatimNodeWithType;

public class DocumentrParserTest {
	private PegDownProcessor processor;

	@Before
	public void setUp() {
		DocumentrParser parser = Parboiled.createParser(DocumentrParser.class);
		processor = new PegDownProcessor(parser);
	}

	@Test
	public void fencedCodeBlock() {
		RootNode rootNode = parse("~~~java:title 123\nxyz\n~~~"); //$NON-NLS-1$
		VerbatimNodeWithType verbatimNode = (VerbatimNodeWithType) rootNode.getChildren().get(0);
		assertEquals("java", verbatimNode.getType()); //$NON-NLS-1$
		assertEquals("title 123", verbatimNode.getTitle()); //$NON-NLS-1$
		assertEquals("xyz", verbatimNode.getText().trim()); //$NON-NLS-1$
	}

	@Test
	public void standaloneMacro() {
		RootNode rootNode = parse("{{foo/}}"); //$NON-NLS-1$
		ParaNode paraNode = (ParaNode) rootNode.getChildren().get(0);
		SuperNode superNode = (SuperNode) paraNode.getChildren().get(0);
		MacroNode macroNode = (MacroNode) superNode.getChildren().get(0);
		assertEquals("foo", macroNode.getMacroName()); //$NON-NLS-1$
		assertNull(macroNode.getParams());
	}

	@Test
	public void standaloneMacroWithParameters() {
		RootNode rootNode = parse("{{foo bar baz/}}"); //$NON-NLS-1$
		ParaNode paraNode = (ParaNode) rootNode.getChildren().get(0);
		SuperNode superNode = (SuperNode) paraNode.getChildren().get(0);
		MacroNode macroNode = (MacroNode) superNode.getChildren().get(0);
		assertEquals("foo", macroNode.getMacroName()); //$NON-NLS-1$
		assertEquals("bar baz", macroNode.getParams()); //$NON-NLS-1$
	}

	@Test
	public void bodyMacro() {
		RootNode rootNode = parse("{{foo}}xyz{{/foo}}"); //$NON-NLS-1$
		ParaNode paraNode = (ParaNode) rootNode.getChildren().get(0);
		SuperNode superNode = (SuperNode) paraNode.getChildren().get(0);
		MacroNode macroNode = (MacroNode) superNode.getChildren().get(0);
		assertEquals("foo", macroNode.getMacroName()); //$NON-NLS-1$
		assertNull(macroNode.getParams());
		paraNode = (ParaNode) macroNode.getChildren().get(0);
		superNode = (SuperNode) paraNode.getChildren().get(0);
		TextNode textNode = (TextNode) superNode.getChildren().get(0);
		assertEquals("xyz", textNode.getText()); //$NON-NLS-1$
	}

	@Test
	public void bodyMacroWithParameters() {
		RootNode rootNode = parse("{{foo bar baz}}xyz{{/foo}}"); //$NON-NLS-1$
		ParaNode paraNode = (ParaNode) rootNode.getChildren().get(0);
		SuperNode superNode = (SuperNode) paraNode.getChildren().get(0);
		MacroNode macroNode = (MacroNode) superNode.getChildren().get(0);
		assertEquals("foo", macroNode.getMacroName()); //$NON-NLS-1$
		assertEquals("bar baz", macroNode.getParams()); //$NON-NLS-1$
		paraNode = (ParaNode) macroNode.getChildren().get(0);
		superNode = (SuperNode) paraNode.getChildren().get(0);
		TextNode textNode = (TextNode) superNode.getChildren().get(0);
		assertEquals("xyz", textNode.getText()); //$NON-NLS-1$
	}

	@Test
	public void pageHeader() {
		RootNode rootNode = parse("{{:header:}}xyz{{:/header:}}"); //$NON-NLS-1$
		ParaNode paraNode = (ParaNode) rootNode.getChildren().get(0);
		SuperNode superNode = (SuperNode) paraNode.getChildren().get(0);
		PageHeaderNode headerNode = (PageHeaderNode) superNode.getChildren().get(0);
		paraNode = (ParaNode) headerNode.getChildren().get(0);
		superNode = (SuperNode) paraNode.getChildren().get(0);
		TextNode textNode = (TextNode) superNode.getChildren().get(0);
		assertEquals("xyz", textNode.getText()); //$NON-NLS-1$
	}

	private RootNode parse(String markdown) {
		return processor.parseMarkdown(markdown.toCharArray());
	}
}
