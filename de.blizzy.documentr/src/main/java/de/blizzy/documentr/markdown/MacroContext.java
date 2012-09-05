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
package de.blizzy.documentr.markdown;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.page.IPageStore;

@Component(MacroContext.ID)
@Scope("prototype")
class MacroContext implements IMacroContext {
	static final String ID = "macroContext"; //$NON-NLS-1$
	
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;
	private String macroName;
	private String parameters;
	private String body;
	private HtmlSerializerContext htmlSerializerContext;

	MacroContext(String macroName, String parameters, String body, HtmlSerializerContext htmlSerializerContext) {
		Assert.hasLength(macroName);
		
		this.macroName = macroName;
		this.parameters = parameters;
		this.body = body;
		this.htmlSerializerContext = htmlSerializerContext;
	}
	
	static MacroContext create(String macroName, String parameters, String body,
			HtmlSerializerContext htmlSerializerContext, BeanFactory beanFactory) {
		
		return (MacroContext) beanFactory.getBean(ID, macroName, parameters, body, htmlSerializerContext);
	}

	@Override
	public String getMacroName() {
		return macroName;
	}

	@Override
	public String getParameters() {
		return parameters;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public HtmlSerializerContext getHtmlSerializerContext() {
		return htmlSerializerContext;
	}

	@Override
	public IPageStore getPageStore() {
		return pageStore;
	}

	@Override
	public DocumentrPermissionEvaluator getPermissionEvaluator() {
		return permissionEvaluator;
	}

	void setPageStore(IPageStore pageStore) {
		this.pageStore = pageStore;
	}

	void setPermissionEvaluator(DocumentrPermissionEvaluator permissionEvaluator) {
		this.permissionEvaluator = permissionEvaluator;
	}
}
