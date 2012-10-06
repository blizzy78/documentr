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
package de.blizzy.documentr.markdown.macro.impl;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Charsets;

import de.blizzy.documentr.markdown.HtmlSerializerContext;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;
import de.blizzy.documentr.markdown.macro.MacroSetting;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.system.SystemSettingsStore;

@Macro(name="flattr", insertText="{{flattr/}}", cacheable=false, settings=@MacroSetting("userId"))
@Slf4j
public class FlattrMacro implements IMacroRunnable {
	@Override
	public String getHtml(IMacroContext macroContext) {
		try {
			HtmlSerializerContext htmlSerializerContext = macroContext.getHtmlSerializerContext();
			SystemSettingsStore systemSettingsStore = htmlSerializerContext.getSystemSettingsStore();
			String userId = systemSettingsStore.getMacroSetting("flattr", "userId"); //$NON-NLS-1$ //$NON-NLS-2$
			if (StringUtils.isNotBlank(userId)) {
				String projectName = htmlSerializerContext.getProjectName();
				String branchName = htmlSerializerContext.getBranchName();
				String path = htmlSerializerContext.getPagePath();
				String pageURI = htmlSerializerContext.getPageURI(path);
				String pageURL = htmlSerializerContext.getURL(pageURI);
				IPageStore pageStore = htmlSerializerContext.getPageStore();
				Page page = pageStore.getPage(projectName, branchName, path, false);
				String title = page.getTitle();
				String tags = StringUtils.join(page.getTags(), ","); //$NON-NLS-1$
				// http://developers.flattr.net/auto-submit/
				UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://flattr.com/submit/auto") //$NON-NLS-1$
					.queryParam("user_id", userId) //$NON-NLS-1$
					.queryParam("url", pageURL) //$NON-NLS-1$
					.queryParam("title", title) //$NON-NLS-1$
					.queryParam("category", "text"); //$NON-NLS-1$ //$NON-NLS-2$
				if (StringUtils.isNotBlank(tags)) {
					builder.queryParam("tags", tags); //$NON-NLS-1$
				}
				String url = builder.build()
					.encode(Charsets.UTF_8.name())
					.toUriString();
				return "<a href=\"" + StringEscapeUtils.escapeHtml4(url) + "\">" + //$NON-NLS-1$ //$NON-NLS-2$
					"<img src=\"https://api.flattr.com/button/flattr-badge-large.png\"/></a>"; //$NON-NLS-1$
			}
		} catch (IOException e) {
			log.warn("error while rendering Flattr macro", e); //$NON-NLS-1$
		}
		return StringUtils.EMPTY;
	}

	@Override
	public String cleanupHTML(String html) {
		return null;
	}
}
