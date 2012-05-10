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
package de.blizzy.documentr.web.markdown.macro;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

class ChildrenMacro implements IMacro {
	private HtmlSerializerContext context;
	private PageStore pageStore;

	@Override
	public void setParameters(String params) {
	}

	@Override
	public void setHtmlSerializerContext(HtmlSerializerContext context) {
		this.context = context;
	}

	@Override
	public void setPageStore(PageStore pageStore) {
		this.pageStore = pageStore;
	}

	@Override
	@SuppressWarnings("nls")
	public String getHtml() {
		try {
			List<String> children = pageStore.listChildPagePaths(context.getProjectName(), context.getBranchName(), context.getPagePath());
			if (!children.isEmpty()) {
				StringBuilder buf = new StringBuilder();
				Page page = pageStore.getPage(context.getProjectName(), context.getBranchName(), context.getPagePath());
				buf.append("<div class=\"children-box\">")
					.append("<ul class=\"children\">")
					.append("<li>").append(page.getTitle()).append("</li>")
					.append("<ul>");
				for (String childPath : children) {
					page = pageStore.getPage(context.getProjectName(), context.getBranchName(), childPath);
					buf.append("<li><a href=\"").append(context.getPageURI(childPath)).append("\">")
						.append(page.getTitle())
						.append("</a></li>");
				}
				buf.append("</ul>")
					.append("</ul></div>");
				return buf.toString();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return StringUtils.EMPTY;
	}
}
