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

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

import com.google.common.eventbus.EventBus;

@Slf4j
class EventBusBeanPostProcessor implements DestructionAwareBeanPostProcessor {
	private BeanFactory beanFactory;

	EventBusBeanPostProcessor(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		log.debug("registering potential event bus subscriber: {}", beanName); //$NON-NLS-1$
		try {
			EventBus eventBus = beanFactory.getBean(EventBus.class);
			eventBus.register(bean);
		} catch (BeanCurrentlyInCreationException e) {
			// ignore beans that are currently in creation (ie. context config)
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

	@Override
	public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
		EventBus eventBus = beanFactory.getBean(EventBus.class);
		eventBus.unregister(bean);
	}
}
