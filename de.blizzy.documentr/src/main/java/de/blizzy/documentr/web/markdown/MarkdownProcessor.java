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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.Parboiled;
import org.pegdown.DocumentrParser;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.Node;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;
import de.blizzy.documentr.web.markdown.macro.MacroInvocation;

@Component
public class MarkdownProcessor {
	static final String NON_CACHEABLE_MACRO_MARKER = MarkdownProcessor.class.getName() + "_NON_CACHEABLE_MACRO"; //$NON-NLS-1$
	static final String NON_CACHEABLE_MACRO_BODY_MARKER = MarkdownProcessor.class.getName() + "_NON_CACHEABLE_MACRO_BODY"; //$NON-NLS-1$

	private static final String TEXT_RANGE_RE = "data-text-range=\"[0-9]+,[0-9]+\""; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final Pattern[] CLEANUP_RE = {
		Pattern.compile("<p>(<p(?!re).*?</p>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p>(<div.*?</div>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p>(<ul.*?</ul>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p>(<ol.*?</ol>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		
		Pattern.compile("<p (" + TEXT_RANGE_RE + ")><div(.*?</div>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p (" + TEXT_RANGE_RE + ")><ul(.*?</ul>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE),
		Pattern.compile("<p (" + TEXT_RANGE_RE + ")><ol(.*?</ol>)</p>", Pattern.DOTALL + Pattern.CASE_INSENSITIVE)
	};
	@SuppressWarnings("nls")
	private static final String[] CLEANUP_REPLACE_WITH = {
		"$1",
		"$1",
		"$1",
		"$1",
		
		"<div $1$2",
		"<ul $1$2",
		"<ol $1$2"
	};
	
	@Autowired
	private MacroFactory macroFactory;

	public String markdownToHTML(String markdown, String projectName, String branchName, String path,
			Authentication authentication) {
		
		return markdownToHTML(markdown, projectName, branchName, path, authentication, true);
	}
	
	public String markdownToHTML(String markdown, String projectName, String branchName, String path,
			Authentication authentication, boolean nonCacheableMacros) {

		return markdownToHTML(markdown, projectName, branchName, path, authentication, nonCacheableMacros, false);
	}
	
	public String headerMarkdownToHTML(String markdown, String projectName, String branchName, String path,
			Authentication authentication) {

		return markdownToHTML(markdown, projectName, branchName, path, authentication, true, true);
	}

	private String markdownToHTML(String markdown, String projectName, String branchName, String path,
			Authentication authentication, boolean nonCacheableMacros, boolean header) {
		
		Parser parser = Parboiled.createParser(DocumentrParser.class);
		PegDownProcessor proc = new PegDownProcessor(parser);
		RootNode rootNode = proc.parseMarkdown(markdown.toCharArray());
		
		fixParaNodes(rootNode);
		
		if (header) {
			extractHeader(rootNode);
		} else {
			removeHeader(rootNode);
		}

		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path, this, authentication);
		HtmlSerializer serializer = new HtmlSerializer(context);
		String html = serializer.toHtml(rootNode);
		
		List<MacroInvocation> macroInvocations = context.getMacroInvocations();
		int nonCacheableMacroIdx = 1;
		for (MacroInvocation invocation : macroInvocations) {
			IMacro macro = invocation.getMacro();
			String startMarker = invocation.getStartMarker();
			String endMarker = invocation.getEndMarker();
			String body = StringUtils.substringBetween(html, startMarker, endMarker);
			if (macro.isCacheable()) {
				String macroHtml = StringUtils.defaultString(macro.getHtml(body));
				html = StringUtils.replace(html, startMarker + body + endMarker, macroHtml);
			} else if (nonCacheableMacros) {
				String macroName = invocation.getMacroName();
				String params = invocation.getParameters();
				String idx = String.valueOf(nonCacheableMacroIdx++);
				html = StringUtils.replace(html, startMarker + body + endMarker,
						"__" + NON_CACHEABLE_MACRO_MARKER + "_" + idx + "__" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						macroName + " " + StringUtils.defaultString(params) + //$NON-NLS-1$
						"__" + NON_CACHEABLE_MACRO_BODY_MARKER + "__" + //$NON-NLS-1$ //$NON-NLS-2$
						body +
						"__/" + NON_CACHEABLE_MACRO_MARKER + "_" + idx + "__"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				html = StringUtils.replace(html, startMarker + body + endMarker, StringUtils.EMPTY);
			}
		}
		html = cleanupHTML(html, macroInvocations, true);
		return html;
	}
	
	private void fixParaNodes(Node node) {
		if ((node instanceof MacroNode) || (node instanceof PageHeaderNode)) {
			List<Node> children = ((SuperNode) node).getChildren();
			if ((children.size() == 1) && (children.get(0) instanceof ParaNode)) {
				List<Node> newChildren = ((ParaNode) children.get(0)).getChildren();
				children.clear();
				children.addAll(newChildren);
			}
		}
		
		if (node instanceof SuperNode) {
			for (Node child : ((SuperNode) node).getChildren()) {
				fixParaNodes(child);
			}
		}
	}
	
	private void extractHeader(RootNode rootNode) {
		List<Node> children = rootNode.getChildren();
		PageHeaderNode headerNode = findHeaderNode(rootNode);
		children.clear();
		if (headerNode != null) {
			children.addAll(headerNode.getChildren());
		}
	}
	
	private PageHeaderNode findHeaderNode(Node node) {
		if (node instanceof PageHeaderNode) {
			return (PageHeaderNode) node;
		}
		
		if (node instanceof SuperNode) {
			for (Node child : ((SuperNode) node).getChildren()) {
				PageHeaderNode headerNode = findHeaderNode(child);
				if (headerNode != null) {
					return headerNode;
				}
			}
		}
		
		return null;
	}
	
	private void removeHeader(Node node) {
		if (node instanceof SuperNode) {
			List<Node> children = ((SuperNode) node).getChildren();
			for (Iterator<Node> iter = children.iterator(); iter.hasNext();) {
				Node child = iter.next();
				if (child instanceof PageHeaderNode) {
					iter.remove();
				}
			}
			
			for (Node child : children) {
				removeHeader(child);
			}
		}
	}

	public String processNonCacheableMacros(String html, String projectName, String branchName, String path,
			Authentication authentication) {
		
		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path, this, authentication);
		String startMarkerPrefix = "__" + NON_CACHEABLE_MACRO_MARKER + "_"; //$NON-NLS-1$ //$NON-NLS-2$
		String endMarkerPrefix = "__/" + NON_CACHEABLE_MACRO_MARKER + "_"; //$NON-NLS-1$ //$NON-NLS-2$
		String bodyMarker = "__" + NON_CACHEABLE_MACRO_BODY_MARKER + "__"; //$NON-NLS-1$ //$NON-NLS-2$
		for (;;) {
			int start = html.indexOf(startMarkerPrefix);
			if (start < 0) {
				break;
			}
			start += startMarkerPrefix.length();
			
			int end = html.indexOf("_", start); //$NON-NLS-1$
			if (end < 0) {
				break;
			}
			String idx = html.substring(start, end);

			start = html.indexOf("__", start); //$NON-NLS-1$
			if (start < 0) {
				break;
			}
			start += 2;
			
			end = html.indexOf(endMarkerPrefix + idx + "__", start); //$NON-NLS-1$
			if (end < 0) {
				break;
			}

			String macroCallWithBody = html.substring(start, end);
			String macroCall = StringUtils.substringBefore(macroCallWithBody, bodyMarker);
			String body = StringUtils.substringAfter(macroCallWithBody, bodyMarker);
			String macroName = StringUtils.substringBefore(macroCall, " "); //$NON-NLS-1$
			String params = StringUtils.substringAfter(macroCall, " "); //$NON-NLS-1$
			IMacro macro = macroFactory.get(macroName, params, context);

			html = StringUtils.replace(html,
					startMarkerPrefix + idx + "__" + macroCallWithBody + endMarkerPrefix + idx + "__", //$NON-NLS-1$ //$NON-NLS-2$
					macro.getHtml(body));

			MacroInvocation invocation = new MacroInvocation(macro, macroName, params);
			html = cleanupHTML(html, Collections.singletonList(invocation), false);
		}
		return html;
	}

	private String cleanupHTML(String html, List<MacroInvocation> macroInvocations, boolean cacheable) {
		for (;;) {
			String newHtml = html;
			for (int i = 0; i < CLEANUP_RE.length; i++) {
				newHtml = CLEANUP_RE[i].matcher(newHtml).replaceAll(CLEANUP_REPLACE_WITH[i]);
			}
			for (MacroInvocation macroInvocation : macroInvocations) {
				IMacro macro = macroInvocation.getMacro();
				if (macro.isCacheable() == cacheable) {
					newHtml = macro.cleanupHTML(newHtml);
				}
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
