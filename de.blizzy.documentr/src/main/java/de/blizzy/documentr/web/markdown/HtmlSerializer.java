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
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.VerbatimNode;

import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;

public class HtmlSerializer extends ToHtmlSerializer {
	private HtmlSerializerContext context;
	private MacroFactory macroFactory;

	public HtmlSerializer(HtmlSerializerContext context, MacroFactory macroFactory) {
		super(new DocumentrLinkRenderer());
		
		this.context = context;
		this.macroFactory = macroFactory;
	}

	@Override
	public void visit(VerbatimNode node) {
		printer.println().print("<pre class=\"pre-scrollable prettyprint linenums\"><code>"); //$NON-NLS-1$
		String text = node.getText();
		while (text.charAt(0) == '\n') {
			printer.print("<br/>"); //$NON-NLS-1$
			text = text.substring(1);
		}
		printer.printEncoded(text);
		printer.print("</code></pre>"); //$NON-NLS-1$
	}
	
	@Override
	protected void printIndentedTag(SuperNode node, String tag) {
		if (tag.equals("table")) { //$NON-NLS-1$
			printer.println().print("<table class=\"table-documentr table-bordered table-striped table-condensed\">").indent(2); //$NON-NLS-1$
			visitChildren(node);
			printer.indent(-2).println().print("</table>"); //$NON-NLS-1$
		} else {
			super.printIndentedTag(node, tag);
		}
	}
	
	@Override
	protected void printImageTag(SuperNode imageNode, String url) {
		printer.print("<img src=\"").print(context.getAttachmentURI(url)) //$NON-NLS-1$
			.print("\" alt=\"").printEncoded(printChildrenToString(imageNode)).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
		if (imageNode instanceof ExpImageNode) {
			String title = ((ExpImageNode) imageNode).title;
			if (StringUtils.isNotBlank(title)) {
				printer.print(" title=\"").printEncoded(title).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		printer.print("/>"); //$NON-NLS-1$
	}
	
	@Override
	public void visit(HeaderNode node) {
		printTag(node, "h" + (node.getLevel() + 1)); //$NON-NLS-1$
	}

	@Override
	public void visit(SuperNode node) {
		if (node instanceof MacroNode) {
			MacroNode macroNode = (MacroNode) node;
			String macroName = macroNode.getMacroName();
			IMacro macro = macroFactory.get(macroName, context);
			printer.print(macro.getHtml());
		} else {
			super.visit(node);
		}
	}
}
