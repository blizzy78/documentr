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

import java.util.Locale;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(MessageSourceMacroDescriptor.ID)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class MessageSourceMacroDescriptor implements IMacroDescriptor {
	static final String ID = "messageSourceMacroDescriptor"; //$NON-NLS-1$
	
	@Autowired
	private MessageSource messageSource;
	private String macroName;
	private String insertText;
	private boolean cacheable = true;

	MessageSourceMacroDescriptor(String macroName) {
		this.macroName = macroName;
	}
	
	static MessageSourceMacroDescriptor create(String macroName, BeanFactory beanFactory) {
		return (MessageSourceMacroDescriptor) beanFactory.getBean(ID, macroName);
	}
	
	@Override
	public String getMacroName() {
		return macroName;
	}

	@Override
	public String getInsertText() {
		return insertText;
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
	
	@Override
	public boolean isCacheable() {
		return cacheable;
	}
	
	MessageSourceMacroDescriptor cacheable(boolean cacheable) {
		this.cacheable = cacheable;
		return this;
	}

	void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}
