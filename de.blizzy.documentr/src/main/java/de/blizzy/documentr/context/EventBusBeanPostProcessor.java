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
package de.blizzy.documentr.context;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

@Slf4j
class EventBusBeanPostProcessor implements BeanPostProcessor {
	private BeanFactory beanFactory;

	EventBusBeanPostProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (isSubscriber(bean)) {
			log.info("registering event bus subscriber: {}", beanName); //$NON-NLS-1$
			EventBus eventBus = beanFactory.getBean(EventBus.class);
			eventBus.register(bean);
		}
		return bean;
	}

	private boolean isSubscriber(Object bean) {
		for (Method method : bean.getClass().getMethods()) {
			Subscribe annotation = method.getAnnotation(Subscribe.class);
			if (annotation != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}
}
