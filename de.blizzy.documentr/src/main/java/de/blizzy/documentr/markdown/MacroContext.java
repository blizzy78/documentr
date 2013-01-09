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
package de.blizzy.documentr.markdown;

import lombok.Getter;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroSettings;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.system.SystemSettingsStore;

@Component(MacroContext.ID)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class MacroContext implements IMacroContext {
	static final String ID = "macroContext"; //$NON-NLS-1$

	@Autowired
	@Getter
	private IPageStore pageStore;
	@Autowired
	@Getter
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Getter
	private String macroName;
	@Getter
	private String parameters;
	@Getter
	private String body;
	@Getter
	private HtmlSerializerContext htmlSerializerContext;
	@Autowired
	private SystemSettingsStore systemSettingsStore;

	MacroContext(String macroName, String parameters, String body, HtmlSerializerContext htmlSerializerContext) {
		Assert.hasLength(macroName);

		this.macroName = macroName;
		this.parameters = parameters;
		this.body = body;
		this.htmlSerializerContext = htmlSerializerContext;
	}

	@Override
	public IMacroSettings getSettings() {
		return new IMacroSettings() {
			@Override
			public String getSetting(String key) {
				return systemSettingsStore.getMacroSetting(macroName, key);
			}
		};
	}

	static MacroContext create(String macroName, String parameters, String body,
			HtmlSerializerContext htmlSerializerContext, BeanFactory beanFactory) {

		return (MacroContext) beanFactory.getBean(ID, macroName, parameters, body, htmlSerializerContext);
	}
}
