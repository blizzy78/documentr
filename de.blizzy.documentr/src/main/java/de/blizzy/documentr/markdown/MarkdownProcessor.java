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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.Parboiled;
import org.pegdown.DocumentrParser;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.Node;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SuperNode;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import de.blizzy.documentr.markdown.macro.IMacro;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.MacroFactory;
import de.blizzy.documentr.markdown.macro.impl.UnknownMacroMacro;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.system.SystemSettingsStore;
import de.blizzy.documentr.util.Replacement;

@Component
public class MarkdownProcessor {
	static final String NON_CACHEABLE_MACRO_MARKER = MarkdownProcessor.class.getName() + "_NON_CACHEABLE_MACRO"; //$NON-NLS-1$
	static final String NON_CACHEABLE_MACRO_BODY_MARKER = MarkdownProcessor.class.getName() + "_NON_CACHEABLE_MACRO_BODY"; //$NON-NLS-1$

	private static final String TEXT_RANGE_RE = "data-text-range=\"[0-9]+,[0-9]+\""; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final List<Replacement> CLEANUP = Lists.newArrayList(
			Replacement.dotAllNoCase("<p>(<p(?!re).*?</p>)</p>", "$1"),
			Replacement.dotAllNoCase("<p>(<div.*?</div>)</p>", "$1"),
			Replacement.dotAllNoCase("<p>(<ul.*?</ul>)</p>", "$1"),
			Replacement.dotAllNoCase("<p>(<ol.*?</ol>)</p>", "$1"),

			Replacement.dotAllNoCase("<p (" + TEXT_RANGE_RE + ")><div(.*?</div>)</p>", "<div $1$2"),
			Replacement.dotAllNoCase("<p (" + TEXT_RANGE_RE + ")><ul(.*?</ul>)</p>", "<ul $1$2"),
			Replacement.dotAllNoCase("<p (" + TEXT_RANGE_RE + ")><ol(.*?</ol>)</p>", "<ol $1$2"),

			Replacement.dotAllNoCase("<p></p>", StringUtils.EMPTY),
			Replacement.dotAllNoCase("(<br/>)+</p>", "</p>"),

			Replacement.dotAllNoCase(
					"(<li class=\"span3\"><a class=\"thumbnail\" (?:[^>]+)>" +
					"<img (?:[^>]+)/></a></li>)</ul>(?:[ \t]|<br/>)*" +
					"<ul class=\"thumbnails\">(<li class=\"span3\">" +
					"<a class=\"thumbnail\" (?:[^>]+)>)",
					"$1$2")
	);

	@Autowired
	private MacroFactory macroFactory;
	@Autowired
	private BeanFactory beanFactory;
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private SystemSettingsStore systemSettingsStore;

	public String markdownToHtml(String markdown, String projectName, String branchName, String path,
			Authentication authentication, String contextPath) {

		return markdownToHtml(markdown, projectName, branchName, path, authentication, true, contextPath);
	}

	public String markdownToHtml(String markdown, String projectName, String branchName, String path,
			Authentication authentication, boolean nonCacheableMacros, String contextPath) {

		RootNode rootNode = parse(markdown);
		removeHeader(rootNode);
		return markdownToHtml(rootNode, projectName, branchName, path, authentication, nonCacheableMacros, contextPath);
	}

	public String headerMarkdownToHtml(String markdown, String projectName, String branchName, String path,
			Authentication authentication, String contextPath) {

		RootNode rootNode = parse(markdown);
		extractHeader(rootNode);
		return markdownToHtml(rootNode, projectName, branchName, path, authentication, true, contextPath);
	}

	private RootNode parse(String markdown) {
		Parser parser = Parboiled.createParser(DocumentrParser.class);
		PegDownProcessor proc = new PegDownProcessor(parser);
		RootNode rootNode = proc.parseMarkdown(markdown.toCharArray());
		fixParaNodes(rootNode);
		return rootNode;
	}

	private String markdownToHtml(RootNode rootNode, String projectName, String branchName, String path,
			Authentication authentication, boolean nonCacheableMacros, String contextPath) {

		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path, this, authentication,
				pageStore, systemSettingsStore, contextPath);
		HtmlSerializer serializer = new HtmlSerializer(context);
		String html = serializer.toHtml(rootNode);

		List<MacroInvocation> macroInvocations = context.getMacroInvocations();
		int nonCacheableMacroIdx = 1;
		for (MacroInvocation invocation : macroInvocations) {
			IMacro macro = macroFactory.get(invocation.getMacroName());
			if (macro == null) {
				macro = new UnknownMacroMacro();
			}
			IMacroDescriptor macroDescriptor = macro.getDescriptor();
			String startMarker = invocation.getStartMarker();
			String endMarker = invocation.getEndMarker();
			String body = StringUtils.substringBetween(html, startMarker, endMarker);
			if (macroDescriptor.isCacheable()) {
				MacroContext macroContext = MacroContext.create(invocation.getMacroName(), invocation.getParameters(),
						body, context, beanFactory);
				IMacroRunnable macroRunnable = macro.createRunnable();
				String macroHtml = StringUtils.defaultString(macroRunnable.getHtml(macroContext));
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
		html = cleanupHtml(html, macroInvocations, true);
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
			Authentication authentication, String contextPath) {

		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, path, this, authentication,
				pageStore, systemSettingsStore, contextPath);
		String startMarkerPrefix = "__" + NON_CACHEABLE_MACRO_MARKER + "_"; //$NON-NLS-1$ //$NON-NLS-2$
		String endMarkerPrefix = "__/" + NON_CACHEABLE_MACRO_MARKER + "_"; //$NON-NLS-1$ //$NON-NLS-2$
		String bodyMarker = "__" + NON_CACHEABLE_MACRO_BODY_MARKER + "__"; //$NON-NLS-1$ //$NON-NLS-2$
		for (;;) {
			int start = html.indexOf(startMarkerPrefix);
			if (start < 0) {
				break;
			}
			start += startMarkerPrefix.length();

			int end = html.indexOf('_', start);
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
			IMacro macro = macroFactory.get(macroName);
			MacroContext macroContext = MacroContext.create(macroName, params, body, context, beanFactory);
			IMacroRunnable macroRunnable = macro.createRunnable();

			html = StringUtils.replace(html,
					startMarkerPrefix + idx + "__" + macroCallWithBody + endMarkerPrefix + idx + "__", //$NON-NLS-1$ //$NON-NLS-2$
					StringUtils.defaultString(macroRunnable.getHtml(macroContext)));

			MacroInvocation invocation = new MacroInvocation(macroName, params);
			html = cleanupHtml(html, Collections.singletonList(invocation), false);
		}
		return html;
	}

	private String cleanupHtml(String html, List<MacroInvocation> macroInvocations, boolean cacheable) {
		for (;;) {
			String newHtml = html;
			for (Replacement replacement : CLEANUP) {
				newHtml = replacement.replaceAll(newHtml);
			}
			for (MacroInvocation macroInvocation : macroInvocations) {
				IMacro macro = macroFactory.get(macroInvocation.getMacroName());
				if (macro != null) {
					IMacroDescriptor macroDescriptor = macro.getDescriptor();
					if (macroDescriptor.isCacheable() == cacheable) {
						IMacroRunnable macroRunnable = macro.createRunnable();
						newHtml = StringUtils.defaultString(macroRunnable.cleanupHtml(newHtml), newHtml);
					}
				}
			}

			if (newHtml.equals(html)) {
				break;
			}

			html = newHtml;
		}
		return html;
	}
}
