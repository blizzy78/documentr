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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

// cannot be a @Component because it must not be picked up by MacroFactory's Collection<IMacro>
class SimpleMacroMacro implements IMacro {
	static final String ID = "simpleMacroMacro"; //$NON-NLS-1$
	
	private ISimpleMacro simpleMacro;
	private Macro annotation;
	private BeanFactory beanFactory;

	SimpleMacroMacro(ISimpleMacro simpleMacro, Macro annotation, BeanFactory beanFactory) {
		Assert.hasLength(annotation.name());
		Assert.hasLength(annotation.insertText());
		
		this.simpleMacro = simpleMacro;
		this.annotation = annotation;
		this.beanFactory = beanFactory;
	}

	@Override
	public IMacroDescriptor getDescriptor() {
		return MessageSourceMacroDescriptor.create(annotation.name(), beanFactory)
				.insertText(annotation.insertText())
				.cacheable(annotation.cacheable());
	}

	@Override
	public IMacroRunnable createRunnable() {
		return simpleMacro.createRunnable();
	}
}
