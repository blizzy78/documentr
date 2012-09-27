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
package de.blizzy.documentr.markdown.macro;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component(MessageSourceMacroDescriptor.ID)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class MessageSourceMacroDescriptor implements IMacroDescriptor {
	static final String ID = "messageSourceMacroDescriptor"; //$NON-NLS-1$
	
	@Autowired
	@Setter(AccessLevel.PACKAGE)
	private MessageSource messageSource;
	@Getter
	private String macroName;
	@Getter
	private String insertText;
	@Getter
	private boolean cacheable = true;
	@Getter
	private Set<MacroSetting> settings = Collections.emptySet();

	MessageSourceMacroDescriptor(String macroName) {
		Assert.hasLength(macroName);
		Assert.isTrue(macroName.indexOf('.') < 0, "macro name contains dots: " + macroName); //$NON-NLS-1$
		
		this.macroName = macroName;
	}
	
	static MessageSourceMacroDescriptor create(String macroName, BeanFactory beanFactory) {
		return (MessageSourceMacroDescriptor) beanFactory.getBean(ID, macroName);
	}
	
	MessageSourceMacroDescriptor insertText(String insertText) {
		this.insertText = insertText;
		return this;
	}
	
	@Override
	public String getTitle(Locale locale) {
		return messageSource.getMessage("macro." + macroName + ".title", null, locale); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getDescription(Locale locale) {
		return messageSource.getMessage("macro." + macroName + ".description", null, locale); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	MessageSourceMacroDescriptor cacheable(boolean cacheable) {
		this.cacheable = cacheable;
		return this;
	}
	
	MessageSourceMacroDescriptor settings(Set<MacroSetting> settings) {
		this.settings = Collections.unmodifiableSet(settings);
		return this;
	}
}
