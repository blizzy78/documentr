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
package de.blizzy.documentr.markdown.macro.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;

@Macro(name="tab", insertText="{{tab TITLE}}[CONTENTS]{{/tab}}")
public class TabMacro implements IMacroRunnable {
	private static final String TAB_START_MARKER = "__TAB__"; //$NON-NLS-1$
	private static final String TAB_END_MARKER = "__/TAB__"; //$NON-NLS-1$

	@Override
	public String getHtml(IMacroContext macroContext) {
		String title = macroContext.getParameters();
		if (StringUtils.isNotBlank(title)) {
			TabData tabData = new TabData(title, macroContext.getBody());
			return TAB_START_MARKER + tabData.serialize() + TAB_END_MARKER;
		}
		return null;
	}

	@Override
	public String cleanupHtml(String html) {
		return null;
	}

	static String getTabsHtml(String html) {
		StringBuilder buf = new StringBuilder();
		int pos = 0;
		for (;;) {
			pos = html.indexOf(TAB_START_MARKER, pos);
			if (pos < 0) {
				break;
			}
			int endPos = html.indexOf(TAB_END_MARKER, pos + TAB_START_MARKER.length());
			if (endPos < 0) {
				break;
			}

			buf.append(html.substring(pos, endPos + TAB_END_MARKER.length()));

			pos = endPos + TAB_END_MARKER.length();
		}
		return buf.toString();
	}

	static List<TabData> getTabs(String s) {
		String[] parts = StringUtils.splitByWholeSeparator(s, TAB_START_MARKER);
		List<TabData> tabs = Lists.newArrayList();
		for (String part : parts) {
			part = StringUtils.removeEnd(part, TAB_END_MARKER);
			if (StringUtils.isNotBlank(part)) {
				TabData tabData = TabData.deserialize(part);
				tabs.add(tabData);
			}
		}
		return tabs;
	}
}
