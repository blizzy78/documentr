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

import de.blizzy.documentr.web.markdown.HtmlSerializerContext;

public abstract class AbstractMacro implements IMacro {
	private String params;
	private HtmlSerializerContext htmlSerializerContext;
	private IMacroContext macroContext;

	@Override
	public void setParameters(String params) {
		this.params = params;
	}
	
	public String getParameters() {
		return params;
	}

	@Override
	public void setHtmlSerializerContext(HtmlSerializerContext htmlSerializerContext) {
		this.htmlSerializerContext = htmlSerializerContext;
	}
	
	public HtmlSerializerContext getHtmlSerializerContext() {
		return htmlSerializerContext;
	}
	
	@Override
	public void setMacroContext(IMacroContext macroContext) {
		this.macroContext = macroContext;
	}
	
	public IMacroContext getMacroContext() {
		return macroContext;
	}

	@Override
	public String cleanupHTML(String html) {
		return html;
	}
	
	@Override
	public boolean isCacheable() {
		return true;
	}
}
