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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import de.blizzy.documentr.AbstractDocumentrTest;

public class EventBusBeanPostProcessorTest extends AbstractDocumentrTest {
	private static final class Subscriber {
		@Subscribe
		@SuppressWarnings("unused")
		public void foo() {}
	}

	private static final class NonSubscriber {}

	@Mock
	private BeanFactory beanFactory;
	@Mock
	private EventBus eventBus;
	@InjectMocks
	private EventBusBeanPostProcessor beanPostProcessor;

	@Before
	public void setUp() {
		when(beanFactory.getBean(EventBus.class)).thenReturn(eventBus);
	}

	@Test
	public void postProcessBeforeInitialization() {
		Subscriber subscriber = new Subscriber();

		Object result = beanPostProcessor.postProcessBeforeInitialization(subscriber, "subscriber"); //$NON-NLS-1$
		assertSame(subscriber, result);

		verify(eventBus).register(subscriber);
	}

	@Test
	public void postProcessBeforeInitializationWithNonSubscriber() {
		NonSubscriber nonSubscriber = new NonSubscriber();

		Object result = beanPostProcessor.postProcessBeforeInitialization(nonSubscriber, "nonSubscriber"); //$NON-NLS-1$
		assertSame(nonSubscriber, result);

		verify(eventBus, never()).register(nonSubscriber);
	}
}
