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
package de.blizzy.documentr.mail;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.common.collect.Maps;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.system.SystemSettingsStore;

public class MailSenderFactoryTest extends AbstractDocumentrTest {
	@Mock
	private SystemSettingsStore systemSettingsStore;
	@InjectMocks
	private MailSenderFactory mailSenderFactory;

	@Test
	public void createSender() {
		Map<String, String> settings = Maps.newHashMap();
		settings.put(SystemSettingsStore.MAIL_HOST_NAME, "host"); //$NON-NLS-1$
		settings.put(SystemSettingsStore.MAIL_HOST_PORT, "25"); //$NON-NLS-1$
		when(systemSettingsStore.getSettings()).thenReturn(settings);
		JavaMailSender sender = mailSenderFactory.createSender();
		assertEquals("host", Whitebox.getInternalState(sender, "host")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals(25, (int) ((Integer) Whitebox.getInternalState(sender, "port"))); //$NON-NLS-1$


		settings.remove(SystemSettingsStore.MAIL_HOST_NAME);
		assertNull(mailSenderFactory.createSender());
	}
}
