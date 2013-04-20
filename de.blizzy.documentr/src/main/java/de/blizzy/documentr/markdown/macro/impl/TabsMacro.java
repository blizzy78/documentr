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
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;

@Macro(name="tabs", insertText="{{tabs}}[CONTENTS]{{/tabs}}")
public class TabsMacro implements IMacroRunnable {
	@Override
	public String getHtml(IMacroContext macroContext) {
		String tabsHtml = TabMacro.getTabsHtml(macroContext.getBody());
		List<TabData> tabs = TabMacro.getTabs(tabsHtml);
		if (!tabs.isEmpty()) {
			Map<TabData, UUID> ids = Maps.newHashMap();
			for (TabData tab : tabs) {
				ids.put(tab, UUID.randomUUID());
			}

			StringBuilder buf = new StringBuilder();
			buf.append("<div class=\"tabbable inline border space\"><ul class=\"nav nav-tabs\">"); //$NON-NLS-1$
			for (TabData tab : tabs) {
				String id = "tab-" + ids.get(tab); //$NON-NLS-1$
				boolean active = tabs.indexOf(tab) == 0;
				buf.append(tab.renderTab(id, active));
			}
			buf.append("</ul><div class=\"tab-content\">"); //$NON-NLS-1$
			for (TabData tab : tabs) {
				String id = "tab-" + ids.get(tab); //$NON-NLS-1$
				boolean active = tabs.indexOf(tab) == 0;
				buf.append(tab.renderContents(id, active));
			}
			buf.append("</div></div>"); //$NON-NLS-1$
			return buf.toString();
		}
		return null;
	}

	@Override
	public String cleanupHtml(String html) {
		return null;
	}
}
