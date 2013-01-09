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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import de.blizzy.documentr.AbstractDocumentrTest;

public class EventBusBeanFactoryPostProcessorTest extends AbstractDocumentrTest {
	@Mock
	private ConfigurableListableBeanFactory beanFactory;

	@Test
	public void postProcessBeanFactory() {
		EventBusBeanFactoryPostProcessor beanFactoryPostProcessor = new EventBusBeanFactoryPostProcessor();
		beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);

		ArgumentCaptor<BeanPostProcessor> captor = ArgumentCaptor.forClass(BeanPostProcessor.class);
		verify(beanFactory).addBeanPostProcessor(captor.capture());
		assertTrue(captor.getValue() instanceof EventBusBeanPostProcessor);
	}
}
