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
	@SuppressWarnings("nls")
	private static final Pattern[] CLEANUP_RE = {
		Pattern.compile("<p>(<p.*?</p>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p>(<div.*?</div>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p>(<ul.*?</ul>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p>(<ol.*?</ol>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE)
	};
	private static final String CLEANUP_REPLACE_WITH = "$1"; //$NON-NLS-1$
	
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
			if (invocation.getMacro().isCacheable()) {
				String macroHtml = StringUtils.defaultString(invocation.getMacro().getHtml());
				html = StringUtils.replace(html, invocation.getMarker(), macroHtml);
			} else {
				html = StringUtils.replace(html, invocation.getMarker(),
						"{{" + invocation.getMacroName() + " " + invocation.getParameters() + "/}}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		html = cleanupHTML(html, macroInvocations);
		return html;
	}
	
	public String processNonCacheableMacros(String html, String projectName, String branchName, String path) {
		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path, this);
		for (;;) {
			int start = html.indexOf("{{") + 2; //$NON-NLS-1$
			if (start < 0) {
				break;
			}
			int end = html.indexOf("/}}", start); //$NON-NLS-1$
			if (end < 0) {
				break;
			}

			String macroCall = html.substring(start, end);
			String macroName = StringUtils.substringBefore(macroCall, " "); //$NON-NLS-1$
			String params = StringUtils.substringAfter(macroCall, " "); //$NON-NLS-1$
			IMacro macro = macroFactory.get(macroName, params, context);
			html = StringUtils.replace(html, "{{" + macroCall + "/}}", macro.getHtml()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return html;
	}

	private String cleanupHTML(String html, List<MacroInvocation> macroInvocations) {
		for (;;) {
			String newHtml = html;
			for (Pattern pattern : CLEANUP_RE) {
				newHtml = pattern.matcher(newHtml).replaceAll(CLEANUP_REPLACE_WITH);
			}
			for (MacroInvocation macroInvocation : macroInvocations) {
				newHtml = macroInvocation.getMacro().cleanupHTML(newHtml);
			}
			
			if (newHtml.equals(html)) {
				break;
			}
			
			html = newHtml;
		}
		return html;
	}
	
	public MacroInvocation getMacroInvocation(String macroName, String params, HtmlSerializerContext context) {
		IMacro macro = macroFactory.get(macroName, params, context);
		return new MacroInvocation(macro, macroName, params);
	}

	void setMacroFactory(MacroFactory macroFactory) {
		this.macroFactory = macroFactory;
	}
}
