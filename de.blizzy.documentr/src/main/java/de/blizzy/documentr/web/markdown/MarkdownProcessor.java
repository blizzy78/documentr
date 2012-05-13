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
import org.parboiled.Parboiled;
import org.pegdown.DocumentrParser;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import de.blizzy.documentr.web.markdown.HtmlSerializer.MacroInvocation;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;

public class MarkdownProcessor {
	private String projectName;
	private String branchName;
	private String path;
	private MacroFactory macroFactory;

	public MarkdownProcessor(String projectName, String branchName, String path, MacroFactory macroFactory) {
		this.projectName = projectName;
		this.branchName = branchName;
		this.path = path;
		this.macroFactory = macroFactory;
	}
	
	public String markdownToHTML(String markdown) {
		Parser parser = Parboiled.createParser(DocumentrParser.class);
		PegDownProcessor proc = new PegDownProcessor(parser);
		RootNode rootNode = proc.parseMarkdown(markdown.toCharArray());
		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path, macroFactory);
		HtmlSerializer serializer = new HtmlSerializer(context);
		List<MacroInvocation> macroInvocations = serializer.getMacroInvocations();
		String html = serializer.toHtml(rootNode);
		for (MacroInvocation invocation : macroInvocations) {
			String macroHtml = invocation.macro.getHtml();
			html = StringUtils.replace(html, invocation.marker, macroHtml);
		}
		html = cleanupHTML(html);
		return html;
	}

	private String cleanupHTML(String html) {
		return html
			.replaceAll("<p>(<div(?:.|[\r\n])*?</div>)</p>", "$1") //$NON-NLS-1$ //$NON-NLS-2$
			.replaceAll("<p>(<ul(?:.|[\r\n])*?</ul>)</p>", "$1") //$NON-NLS-1$ //$NON-NLS-2$
			.replaceAll("<p>(<ol(?:.|[\r\n])*?</ol>)</p>", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
