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
package de.blizzy.documentr.markdown.macro;

import java.util.Collections;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

import com.google.common.collect.Sets;

// cannot be a @Component because it must not be picked up by MacroFactory
class MacroRunnableMacro implements IMacro {
	private Class<? extends IMacroRunnable> clazz;
	private Macro annotation;
	private BeanFactory beanFactory;

	MacroRunnableMacro(Class<? extends IMacroRunnable> clazz, Macro annotation, BeanFactory beanFactory) {
		Assert.notNull(clazz);
		Assert.notNull(annotation);
		Assert.hasLength(annotation.name());
		Assert.hasLength(annotation.insertText());
		Assert.notNull(beanFactory);

		this.clazz = clazz;
		this.annotation = annotation;
		this.beanFactory = beanFactory;
	}

	@Override
	public IMacroDescriptor getDescriptor() {
		MacroSetting[] settings = annotation.settings();
		return MessageSourceMacroDescriptor.create(annotation.name(), beanFactory)
				.insertText(annotation.insertText())
				.cacheable(annotation.cacheable())
				.settings(settings != null ? Sets.newHashSet(settings) : Collections.<MacroSetting>emptySet());
	}

	@Override
	public IMacroRunnable createRunnable() {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
