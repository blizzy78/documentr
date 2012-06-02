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
package de.blizzy.documentr.web.markdown.macro.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.pagestore.IPageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;
import de.blizzy.documentr.web.markdown.macro.IMacro;
import de.blizzy.documentr.web.markdown.macro.IMacroContext;

@Component
public class MacroFactory {
	private static final Map<String, Class<? extends IMacro>> MACRO_CLASSES =
			new HashMap<String, Class<? extends IMacro>>();
	
	static {
		MACRO_CLASSES.put("neighbors", NeighborsMacro.class); //$NON-NLS-1$
		MACRO_CLASSES.put("neighbours", NeighborsMacro.class); //$NON-NLS-1$
		MACRO_CLASSES.put("toc", TableOfContentsMacro.class); //$NON-NLS-1$
	}
	
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;
	
	public IMacro get(String macroName, String params, HtmlSerializerContext context) {
		try {
			Class<? extends IMacro> clazz = MACRO_CLASSES.get(macroName);
			IMacro macro;
			if (clazz != null) {
				macro = clazz.newInstance();
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
	
	void setPageStore(IPageStore pageStore) {
		this.pageStore = pageStore;
	}

	void setPermissionEvaluator(DocumentrPermissionEvaluator permissionEvaluator) {
		this.permissionEvaluator = permissionEvaluator;
	}
}
