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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;

import de.blizzy.documentr.Util;
import de.blizzy.documentr.web.markdown.macro.IMacro;

public class HtmlSerializer extends ToHtmlSerializer {
	static final class MacroInvocation {
		final IMacro macro;
		final String marker;

		private MacroInvocation(IMacro macro) {
			this.macro = macro;
			
			marker = "__" + macro.getClass().getName() + "_" + System.currentTimeMillis() + "__"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}
	
	private HtmlSerializerContext context;
	private List<MacroInvocation> macroInvocations = new ArrayList<MacroInvocation>();

	public HtmlSerializer(HtmlSerializerContext context) {
		super(new DocumentrLinkRenderer());
		
		this.context = context;
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
		List<Node> children = node.getChildren();
		if (!children.isEmpty()) {
			Node childNode = children.get(0);
			if (childNode instanceof TextNode) {
				TextNode textNode = (TextNode) childNode;
				String text = textNode.getText();
				String anchor = Util.simplifyForURL(text);
				printer.print("<a name=\"").print(anchor).print("\"></a>"); //$NON-NLS-1$ //$NON-NLS-2$
				context.addHeader(text, node.getLevel());
			}
		}
		printTag(node, "h" + (node.getLevel() + 1)); //$NON-NLS-1$
	}

	@Override
	public void visit(SuperNode node) {
		if (node instanceof MacroNode) {
			MacroNode macroNode = (MacroNode) node;
			String macroName = macroNode.getMacroName();
			IMacro macro = context.getMacroFactory().get(macroName, context);
			MacroInvocation invocation = new MacroInvocation(macro);
			macroInvocations.add(invocation);
			printer.print(invocation.marker);
		} else {
			super.visit(node);
		}
	}
	
	List<MacroInvocation> getMacroInvocations() {
		return macroInvocations;
	}
}
