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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

@Component
public class MacroFactory {
	private static final Map<String, Class<? extends IMacro>> MACRO_CLASSES =
			new HashMap<String, Class<? extends IMacro>>();
	
	@Autowired
	private PageStore pageStore;

	static {
		MACRO_CLASSES.put("neighbors", NeighborsMacro.class); //$NON-NLS-1$
		MACRO_CLASSES.put("neighbours", NeighborsMacro.class); //$NON-NLS-1$
		MACRO_CLASSES.put("toc", TableOfContentsMacro.class); //$NON-NLS-1$
	}
	
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
			macro.setPageStore(pageStore);
			return macro;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
