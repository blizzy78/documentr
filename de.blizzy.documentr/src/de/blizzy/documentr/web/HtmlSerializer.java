package de.blizzy.documentr.web;

import org.apache.commons.lang3.StringUtils;
import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.VerbatimNode;

public class HtmlSerializer extends ToHtmlSerializer {
	private HtmlSerializerContext context;

	HtmlSerializer(HtmlSerializerContext context) {
		super(new LinkRenderer());
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
}
