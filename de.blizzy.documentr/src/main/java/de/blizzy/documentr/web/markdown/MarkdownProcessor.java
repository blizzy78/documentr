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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.Parboiled;
import org.pegdown.DocumentrParser;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;
import de.blizzy.documentr.web.markdown.macro.MacroInvocation;

@Component
public class MarkdownProcessor {
	@Autowired
	private MacroFactory macroFactory;

	public String markdownToHTML(String markdown, String projectName, String branchName, String path) {
		Parser parser = Parboiled.createParser(DocumentrParser.class);
		PegDownProcessor proc = new PegDownProcessor(parser);
		RootNode rootNode = proc.parseMarkdown(markdown.toCharArray());

		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path, this);
		HtmlSerializer serializer = new HtmlSerializer(context);
		String html = serializer.toHtml(rootNode);
		
		List<MacroInvocation> macroInvocations = context.getMacroInvocations();
		for (MacroInvocation invocation : macroInvocations) {
			String macroHtml = StringUtils.defaultString(invocation.getMacro().getHtml());
			html = StringUtils.replace(html, invocation.getMarker(), macroHtml);
		}
		html = cleanupHTML(html);
		return html;
	}

	private String cleanupHTML(String html) {
		for (;;) {
			String newHtml = html;
			newHtml = replace(newHtml, "<p>(<p.*?</p>)</p>", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			newHtml = replace(newHtml, "<p>(<div.*?</div>)</p>", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			newHtml = replace(newHtml, "<p>(<ul.*?</ul>)</p>", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			newHtml = replace(newHtml, "<p>(<ol.*?</ol>)</p>", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
			
			if (newHtml.equals(html)) {
				break;
			}
			
			html = newHtml;
		}
		return html;
	}
	
	private String replace(String html, String pattern, String replaceWith) {
		Pattern p = Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(html);
		return matcher.replaceAll(replaceWith);
	}

	public MacroInvocation getMacroInvocation(String macroName, String params, HtmlSerializerContext context) {
		IMacro macro = macroFactory.get(macroName, params, context);
		return new MacroInvocation(macro);
	}

	void setMacroFactory(MacroFactory macroFactory) {
		this.macroFactory = macroFactory;
	}
}
