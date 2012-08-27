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
package de.blizzy.documentr.web.markdown.macro.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;
import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.IMacroContext;
import de.blizzy.documentr.web.markdown.macro.MacroDescriptor;
import de.blizzy.documentr.web.markdown.macro.impl.AlertMacro;
import de.blizzy.documentr.web.markdown.macro.impl.ColorMacro;
import de.blizzy.documentr.web.markdown.macro.impl.GoogleDocsMacro;
import de.blizzy.documentr.web.markdown.macro.impl.LabelMacro;
import de.blizzy.documentr.web.markdown.macro.impl.NeighborsMacro;
import de.blizzy.documentr.web.markdown.macro.impl.PanelMacro;
import de.blizzy.documentr.web.markdown.macro.impl.PanelRowMacro;
import de.blizzy.documentr.web.markdown.macro.impl.TableOfContentsMacro;
import de.blizzy.documentr.web.markdown.macro.impl.TwitterMacro;
import de.blizzy.documentr.web.markdown.macro.impl.UnknownMacroMacro;
import de.blizzy.documentr.web.markdown.macro.impl.VimeoMacro;
import de.blizzy.documentr.web.markdown.macro.impl.YoutubeMacro;

@Component
public class MacroFactory {
	private static final Map<String, MacroDescriptor> MACROS = new HashMap<String, MacroDescriptor>();
	
	static {
		put(AlertMacro.DESCRIPTOR);
		put(ColorMacro.DESCRIPTOR);
		put(GoogleDocsMacro.DESCRIPTOR);
		put(LabelMacro.DESCRIPTOR);
		put(NeighborsMacro.DESCRIPTOR);
		put(PanelMacro.DESCRIPTOR);
		put(PanelRowMacro.DESCRIPTOR);
		put(TableOfContentsMacro.DESCRIPTOR);
		put(TwitterMacro.DESCRIPTOR);
		put(VimeoMacro.DESCRIPTOR);
		put(YoutubeMacro.DESCRIPTOR);
	}
	
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;

	private static void put(MacroDescriptor desc) {
		MACROS.put(desc.getName(), desc);
	}
	
	public IMacro get(String macroName, String params, HtmlSerializerContext context) {
		try {
			MacroDescriptor desc = MACROS.get(macroName);
			IMacro macro;
			if (desc != null) {
				macro = desc.getMacroClass().newInstance();
			} else {
				macro = new UnknownMacroMacro(macroName);
			}
			macro.setParameters(params);
			macro.setHtmlSerializerContext(context);
			IMacroContext macroContext = new MacroContext(pageStore, permissionEvaluator);
			macro.setMacroContext(macroContext);
			return macro;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Set<MacroDescriptor> getDescriptors() {
		return Sets.newHashSet(MACROS.values());
	}

	void setPageStore(IPageStore pageStore) {
		this.pageStore = pageStore;
	}

	void setPermissionEvaluator(DocumentrPermissionEvaluator permissionEvaluator) {
		this.permissionEvaluator = permissionEvaluator;
	}
}
