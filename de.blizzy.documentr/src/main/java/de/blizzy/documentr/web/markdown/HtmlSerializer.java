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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.VerbatimNode;

import de.blizzy.documentr.Util;
import de.blizzy.documentr.web.markdown.macro.MacroInvocation;

public class HtmlSerializer extends ToHtmlSerializer {
	private static final String IMAGE_PARAM_THUMB = "thumb"; //$NON-NLS-1$
	
	private HtmlSerializerContext context;

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
		String params = StringUtils.EMPTY;
		if (url.contains("|")) { //$NON-NLS-1$
			params = StringUtils.substringAfter(url, "|").trim(); //$NON-NLS-1$
			url = StringUtils.substringBefore(url, "|").trim(); //$NON-NLS-1$
		}
		
		boolean thumbnail = params.contains(IMAGE_PARAM_THUMB);
		
		if (thumbnail) {
			printer.print("<ul class=\"thumbnails\"><li class=\"span3\"><a class=\"thumbnail\" ") //$NON-NLS-1$
				.print("rel=\"lightbox[images]\" href=\"") //$NON-NLS-1$
				.print(context.getAttachmentURI(url)).print("\">"); //$NON-NLS-1$
		}
		
		String altText = printChildrenToString(imageNode);
		printer.print("<img src=\"").print(context.getAttachmentURI(url)).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
		if (StringUtils.isNotBlank(altText)) {
			printer.print(" alt=\"").printEncoded(altText).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (imageNode instanceof ExpImageNode) {
			String title = ((ExpImageNode) imageNode).title;
			if (StringUtils.isNotBlank(title)) {
				printer.print(" title=\"").printEncoded(title).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (thumbnail) {
			printer.print(" width=\"260\""); //$NON-NLS-1$
		}
		printer.print("/>"); //$NON-NLS-1$
		
		if (thumbnail) {
			printer.print("</a></li></ul>"); //$NON-NLS-1$
		}
	}
	
	@Override
	public void visit(HeaderNode node) {
		List<Node> children = node.getChildren();
		if (!children.isEmpty()) {
			String text = printChildrenToString(node);
			if (StringUtils.isNotBlank(text)) {
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
			String params = macroNode.getParams();
			MacroInvocation invocation = context.addMacroInvocation(macroName, params);
			printer.print(invocation.getMarker());
		} else {
			super.visit(node);
		}
	}
}
