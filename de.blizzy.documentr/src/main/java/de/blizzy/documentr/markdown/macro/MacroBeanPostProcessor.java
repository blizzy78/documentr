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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MacroBeanPostProcessor implements BeanPostProcessor {
	@Autowired
	private BeanFactory beanFactory;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		Macro annotation = bean.getClass().getAnnotation(Macro.class);
		if (annotation != null) {
			if (StringUtils.isBlank(annotation.name())) {
				throw new BeanDefinitionValidationException("no macro name specified in @" + //$NON-NLS-1$
						Macro.class.getSimpleName() + " annotation for bean: " + beanName); //$NON-NLS-1$
			}

			if (!(bean instanceof IMacro)) {
				if (bean instanceof ISimpleMacro) {
					return createMacro((ISimpleMacro) bean, annotation);
				} else if (bean instanceof IMacroRunnable) {
					@SuppressWarnings("unchecked")
					Class<? extends IMacroRunnable> clazz = (Class<? extends IMacroRunnable>) bean.getClass();
					return createMacro(clazz, annotation);
				} else {
					throw new BeanNotOfRequiredTypeException(beanName, ISimpleMacro.class, bean.getClass());
				}
			} else {
				throw new BeanNotOfRequiredTypeException(beanName, ISimpleMacro.class, bean.getClass());
			}
		} else {
			return bean;
		}
	}

	private IMacro createMacro(ISimpleMacro bean, Macro annotation) {
		log.info("creating macro from simple macro: {}", annotation.name()); //$NON-NLS-1$
		return new SimpleMacroMacro(bean, annotation, beanFactory);
	}

	private IMacro createMacro(Class<? extends IMacroRunnable> clazz, Macro annotation) {
		log.info("creating macro from macro runnable: {}", annotation.name()); //$NON-NLS-1$
		return new MacroRunnableMacro(clazz, annotation, beanFactory);
	}
}
