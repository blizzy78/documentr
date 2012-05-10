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

import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

class UnknownMacroMacro implements IMacro {
	private String macroName;

	public UnknownMacroMacro(String macroName) {
		this.macroName = macroName;
	}

	@Override
	public void setHtmlSerializerContext(HtmlSerializerContext context) {
	}
	
	@Override
	public void setPageStore(PageStore pageStore) {
	}
	
	@Override
	public void setParameters(String params) {
	}

	@Override
	public String getHtml() {
		return "<span class=\"unknown-macro\">!" + macroName + "!</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
