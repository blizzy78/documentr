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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.beans.factory.BeanFactory;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.system.SystemSettingsStore;

public class MacroContextTest extends AbstractDocumentrTest {
	private static final String MACRO = "macro"; //$NON-NLS-1$

	@Mock
	private BeanFactory beanFactory;
	@Mock
	private IPageStore pageStore;
	@Mock
	private DocumentrPermissionEvaluator permissionEvaluator;
	@Mock
	private HtmlSerializerContext htmlSerializerContext;
	@Mock
	private SystemSettingsStore systemSettingsStore;
	private MacroContext context;

	@Before
	public void setUp() {
		MacroContext ctx = new MacroContext(MACRO, "params", "body", htmlSerializerContext); //$NON-NLS-1$ //$NON-NLS-2$
		Whitebox.setInternalState(ctx, pageStore, permissionEvaluator, systemSettingsStore);

		when(beanFactory.getBean(MacroContext.ID, MACRO, "params", "body", htmlSerializerContext)) //$NON-NLS-1$ //$NON-NLS-2$
			.thenReturn(ctx);

		context = MacroContext.create(MACRO, "params", "body", htmlSerializerContext, beanFactory); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void getSettings() {
		when(systemSettingsStore.getMacroSetting(MACRO, "key")).thenReturn("value"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("value", context.getSettings().getSetting("key")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
