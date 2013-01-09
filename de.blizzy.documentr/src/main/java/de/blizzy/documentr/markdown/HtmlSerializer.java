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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.VerbatimNode;

import de.blizzy.documentr.util.Util;

public class HtmlSerializer extends ToHtmlSerializer {
	private static final String IMAGE_PARAM_THUMB = "thumb"; //$NON-NLS-1$

	private HtmlSerializerContext context;

	public HtmlSerializer(HtmlSerializerContext context) {
		super(new DocumentrLinkRenderer(context));

		this.context = context;

		printer = new DocumentrPrinter();
	}

	@Override
	public void visit(ParaNode node) {
		printTagWithTextRange(node, "p"); //$NON-NLS-1$
	}

	@Override
	public void visit(VerbatimNode node) {
		if (node instanceof VerbatimNodeWithType) {
			String title = ((VerbatimNodeWithType) node).getTitle();
			if (StringUtils.isNotBlank(title)) {
				printer.print("<div class=\"code-view-title\">") //$NON-NLS-1$
					.printEncoded(title.trim())
					.print("</div>"); //$NON-NLS-1$
			}
		}
		printer.print("<div class=\"code-view-wrapper\">" + //$NON-NLS-1$
				"<!--__NOTRIM__--><div class=\"code-view\" data-text-range=\"") //$NON-NLS-1$
			.print(String.valueOf(node.getStartIndex())).print(",").print(String.valueOf(node.getEndIndex())) //$NON-NLS-1$
			.print("\""); //$NON-NLS-1$
		if (node instanceof VerbatimNodeWithType) {
			String type = ((VerbatimNodeWithType) node).getType();
			if (StringUtils.isNotBlank(type)) {
				printer.print(" data-type=\"").printEncoded(type.trim()).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		printer.print(">") //$NON-NLS-1$
			.printEncoded(node.getText().replaceFirst("[\\r\\n]*$", StringUtils.EMPTY)); //$NON-NLS-1$
		printer.print("</div><!--__/NOTRIM__--></div>\n"); //$NON-NLS-1$
	}

	@Override
	protected void printIndentedTag(SuperNode node, String tag) {
		if (tag.equals("table")) { //$NON-NLS-1$
			printer.print("<table class=\"table-documentr table-bordered table-striped " + //$NON-NLS-1$
					"table-condensed\" data-text-range=\"").print(String.valueOf(node.getStartIndex())) //$NON-NLS-1$
					.print(",").print(String.valueOf(node.getEndIndex())).print("\">"); //$NON-NLS-1$ //$NON-NLS-2$
			visitChildren(node);
			printer.print("</table>"); //$NON-NLS-1$
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
		String altText = printChildrenToString(imageNode);
		String title = null;
		if (imageNode instanceof ExpImageNode) {
			title = StringUtils.defaultIfBlank(((ExpImageNode) imageNode).title, altText);
		}

		if (thumbnail) {
			printer.print("<ul class=\"thumbnails\"><li class=\"span3\"><a class=\"thumbnail\" href=\"") //$NON-NLS-1$
				.print(context.getAttachmentUri(url)).print("\">"); //$NON-NLS-1$
		}

		printer.print("<img src=\"").print(context.getAttachmentUri(url)).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
		if (StringUtils.isNotBlank(altText)) {
			printer.print(" alt=\"").printEncoded(altText).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (thumbnail) {
			printer.print(" data-lightbox=\"lightbox\" width=\"260\""); //$NON-NLS-1$
		}
		if (StringUtils.isNotBlank(title)) {
			printer.print(" rel=\"tooltip\" data-title=\"").printEncoded(title).print("\""); //$NON-NLS-1$ //$NON-NLS-2$
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
				String anchor = Util.simplifyForUrl(text);
				printer.print("<a name=\"").print(anchor).print("\"></a>"); //$NON-NLS-1$ //$NON-NLS-2$
				context.addHeader(text, node.getLevel());
			}
		}
		printTagWithTextRange(node, "h" + (node.getLevel() + 1)); //$NON-NLS-1$
	}

	@Override
	public void visit(BulletListNode node) {
		printTagWithTextRange(node, "ul"); //$NON-NLS-1$
	}

	@Override
	public void visit(OrderedListNode node) {
		printTagWithTextRange(node, "ol"); //$NON-NLS-1$
	}

	@Override
	public void visit(DefinitionListNode node) {
		printTagWithTextRange(node, "dl"); //$NON-NLS-1$
	}

	private void printTagWithTextRange(SuperNode node, String tag) {
		printer.print("<").print(tag).print(" data-text-range=\"") //$NON-NLS-1$ //$NON-NLS-2$
			.print(String.valueOf(node.getStartIndex())).print(",").print(String.valueOf(node.getEndIndex())) //$NON-NLS-1$
			.print("\">"); //$NON-NLS-1$
		visitChildren(node);
		printer.print("</").print(tag).print(">"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void visit(SuperNode node) {
		if (node instanceof MacroNode) {
			MacroNode macroNode = (MacroNode) node;
			String macroName = macroNode.getMacroName();
			String params = macroNode.getParams();
			MacroInvocation invocation = context.addMacroInvocation(macroName, params);
			printer.print(invocation.getStartMarker());
			visitChildren(macroNode);
			printer.print(invocation.getEndMarker());
		} else {
			super.visit(node);
		}
	}
}
