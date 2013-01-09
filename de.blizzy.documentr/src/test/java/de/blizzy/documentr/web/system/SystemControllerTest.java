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
package de.blizzy.documentr.web.system;

import static de.blizzy.documentr.TestUtil.*;
import static junit.framework.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.WebRequest;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.MacroFactory;
import de.blizzy.documentr.markdown.macro.MacroSetting;
import de.blizzy.documentr.system.SystemSettingsStore;

public class SystemControllerTest extends AbstractDocumentrTest {
	private static final String DOCUMENTR_HOST = "documentrHost"; //$NON-NLS-1$
	private static final String SITE_NOTICE = "siteNotice"; //$NON-NLS-1$
	private static final String MAIL_HOST_NAME = "mailHostName"; //$NON-NLS-1$
	private static final int MAIL_HOST_PORT = 123;
	private static final String MAIL_SENDER_EMAIL = "mailSenderEmail"; //$NON-NLS-1$
	private static final String MAIL_SENDER_NAME = "mailSenderName"; //$NON-NLS-1$
	private static final String MAIL_SUBJECT_PREFIX = "mailSubjectPrefix"; //$NON-NLS-1$
	private static final String MAIL_DEFAULT_LANGUAGE = Locale.ENGLISH.getLanguage();
	private static final int BCRYPT_ROUNDS = 234;
	private static final String PAGE_FOOTER_HTML = "pageFooterHtml"; //$NON-NLS-1$
	private static final String UPDATE_CHECK_INTERVAL = SystemSettingsStore.UPDATE_CHECK_INTERVAL_DAILY;

	@Mock
	private SystemSettingsStore systemSettingsStore;
	@Mock
	private UserStore userStore;
	@Mock
	private MacroFactory macroFactory;
	@Mock
	private IMacroDescriptor macroDescriptor1;
	@Mock
	private IMacroDescriptor macroDescriptor2;
	@Mock
	private MacroSetting setting1;
	@Mock
	private MacroSetting setting2;
	@Mock
	private Model model;
	@Mock
	private User user;
	@Mock
	private Authentication authentication;
	@Mock
	private BindingResult bindingResult;
	@Mock
	private WebRequest webRequest;
	@InjectMocks
	private SystemController systemController;

	@Before
	public void setUp() {
	}

	@Test
	public void editSettings() {
		when(macroDescriptor1.getMacroName()).thenReturn("macro1"); //$NON-NLS-1$
		when(setting1.value()).thenReturn("key1"); //$NON-NLS-1$
		when(macroDescriptor1.getSettings()).thenReturn(Sets.newHashSet(setting1));

		when(macroDescriptor2.getMacroName()).thenReturn("macro2"); //$NON-NLS-1$
		when(setting2.value()).thenReturn("key2"); //$NON-NLS-1$
		when(macroDescriptor2.getSettings()).thenReturn(Sets.newHashSet(setting2));

		when(macroFactory.getDescriptors()).thenReturn(Sets.newHashSet(macroDescriptor1, macroDescriptor2));

		when(systemSettingsStore.getMacroSetting("macro1", "key1")).thenReturn("macroValue1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		when(systemSettingsStore.getMacroSetting("macro2", "key2")).thenReturn("macroValue2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		Map<String, String> settings = Maps.newHashMap();
		settings.put(SystemSettingsStore.DOCUMENTR_HOST, DOCUMENTR_HOST);
		settings.put(SystemSettingsStore.SITE_NOTICE, SITE_NOTICE);
		settings.put(SystemSettingsStore.MAIL_HOST_NAME, MAIL_HOST_NAME);
		settings.put(SystemSettingsStore.MAIL_HOST_PORT, "123"); //$NON-NLS-1$
		settings.put(SystemSettingsStore.MAIL_SENDER_EMAIL, MAIL_SENDER_EMAIL);
		settings.put(SystemSettingsStore.MAIL_SENDER_NAME, MAIL_SENDER_NAME);
		settings.put(SystemSettingsStore.MAIL_SUBJECT_PREFIX, MAIL_SUBJECT_PREFIX);
		settings.put(SystemSettingsStore.MAIL_DEFAULT_LANGUAGE, MAIL_DEFAULT_LANGUAGE);
		settings.put(SystemSettingsStore.BCRYPT_ROUNDS, "234"); //$NON-NLS-1$
		settings.put(SystemSettingsStore.PAGE_FOOTER_HTML, PAGE_FOOTER_HTML);
		settings.put(SystemSettingsStore.UPDATE_CHECK_INTERVAL, UPDATE_CHECK_INTERVAL);
		when(systemSettingsStore.getSettings()).thenReturn(settings);

		String view = systemController.editSettings(model);
		assertEquals("/system/edit", view); //$NON-NLS-1$

		ArgumentCaptor<SystemSettingsForm> captor = ArgumentCaptor.forClass(SystemSettingsForm.class);
		verify(model).addAttribute(eq("systemSettingsForm"), captor.capture()); //$NON-NLS-1$
		SystemSettingsForm form = captor.getValue();
		assertEquals(DOCUMENTR_HOST, form.getDocumentrHost());
		assertEquals(SITE_NOTICE, form.getSiteNotice());
		assertEquals(MAIL_HOST_NAME, form.getMailHostName());
		assertEquals(123, form.getMailHostPort());
		assertEquals(MAIL_SENDER_EMAIL, form.getMailSenderEmail());
		assertEquals(MAIL_SENDER_NAME, form.getMailSenderName());
		assertEquals(MAIL_SUBJECT_PREFIX, form.getMailSubjectPrefix());
		assertEquals(MAIL_DEFAULT_LANGUAGE, form.getMailDefaultLanguage());
		assertEquals(234, form.getBcryptRounds());
		assertEquals(PAGE_FOOTER_HTML, form.getPageFooterHtml());
		assertEquals(UPDATE_CHECK_INTERVAL, form.getUpdateCheckInterval());
		assertEquals("macroValue1", form.getMacroSettings().get("macro1").get("key1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("macroValue2", form.getMacroSettings().get("macro2").get("key2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void saveSettings() throws IOException {
		when(authentication.getName()).thenReturn("user"); //$NON-NLS-1$

		when(userStore.getUser("user")).thenReturn(user); //$NON-NLS-1$

		SortedMap<String, SortedMap<String, String>> allMacroSettings = Maps.newTreeMap();
		SortedMap<String, String> macroSettings = Maps.newTreeMap();
		macroSettings.put("key1", "macroValue1"); //$NON-NLS-1$ //$NON-NLS-2$
		allMacroSettings.put("macro1", macroSettings); //$NON-NLS-1$
		macroSettings = Maps.newTreeMap();
		macroSettings.put("key2", "macroValue2"); //$NON-NLS-1$ //$NON-NLS-2$
		allMacroSettings.put("macro2", macroSettings); //$NON-NLS-1$
		SystemSettingsForm form = new SystemSettingsForm(DOCUMENTR_HOST, SITE_NOTICE, MAIL_HOST_NAME, 123, MAIL_SENDER_EMAIL,
				MAIL_SENDER_NAME, MAIL_SUBJECT_PREFIX, MAIL_DEFAULT_LANGUAGE, 234, PAGE_FOOTER_HTML, UPDATE_CHECK_INTERVAL,
				allMacroSettings);

		String view = systemController.saveSettings(form, bindingResult, authentication);
		assertEquals("/system/edit", removeViewPrefix(view)); //$NON-NLS-1$
		assertRedirect(view);

		Map<String, String> settings = Maps.newHashMap();
		settings.put(SystemSettingsStore.DOCUMENTR_HOST, DOCUMENTR_HOST);
		settings.put(SystemSettingsStore.SITE_NOTICE, SITE_NOTICE);
		settings.put(SystemSettingsStore.MAIL_HOST_NAME, MAIL_HOST_NAME);
		settings.put(SystemSettingsStore.MAIL_HOST_PORT, "123"); //$NON-NLS-1$
		settings.put(SystemSettingsStore.MAIL_SENDER_EMAIL, MAIL_SENDER_EMAIL);
		settings.put(SystemSettingsStore.MAIL_SENDER_NAME, MAIL_SENDER_NAME);
		settings.put(SystemSettingsStore.MAIL_SUBJECT_PREFIX, MAIL_SUBJECT_PREFIX);
		settings.put(SystemSettingsStore.MAIL_DEFAULT_LANGUAGE, MAIL_DEFAULT_LANGUAGE);
		settings.put(SystemSettingsStore.BCRYPT_ROUNDS, "234"); //$NON-NLS-1$
		settings.put(SystemSettingsStore.PAGE_FOOTER_HTML, PAGE_FOOTER_HTML);
		settings.put(SystemSettingsStore.UPDATE_CHECK_INTERVAL, UPDATE_CHECK_INTERVAL);
		verify(systemSettingsStore).saveSettings(settings, user);

		macroSettings = Maps.newTreeMap();
		macroSettings.put("key1", "macroValue1"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(systemSettingsStore).setMacroSetting("macro1", macroSettings, user); //$NON-NLS-1$
		macroSettings = Maps.newTreeMap();
		macroSettings.put("key2", "macroValue2"); //$NON-NLS-1$ //$NON-NLS-2$
		verify(systemSettingsStore).setMacroSetting("macro2", macroSettings, user); //$NON-NLS-1$
	}

	@Test
	public void createSystemSettingsForm() {
		Map<String, String[]> params = Maps.newHashMap();
		params.put("macro.macro1.key1", new String[] { "macroValue1" }); //$NON-NLS-1$ //$NON-NLS-2$
		params.put("macro.macro2.key2", new String[] { "macroValue2" }); //$NON-NLS-1$ //$NON-NLS-2$
		when(webRequest.getParameterMap()).thenReturn(params);

		SystemSettingsForm form = systemController.createSystemSettingsForm(DOCUMENTR_HOST,
				SITE_NOTICE, MAIL_HOST_NAME, 123, MAIL_SENDER_EMAIL, MAIL_SENDER_NAME,
				MAIL_SUBJECT_PREFIX, MAIL_DEFAULT_LANGUAGE, 234, PAGE_FOOTER_HTML,
				UPDATE_CHECK_INTERVAL, webRequest);
		assertEquals(DOCUMENTR_HOST, form.getDocumentrHost());
		assertEquals(SITE_NOTICE, form.getSiteNotice());
		assertEquals(MAIL_HOST_NAME, form.getMailHostName());
		assertEquals(MAIL_HOST_PORT, form.getMailHostPort());
		assertEquals(MAIL_SENDER_EMAIL, form.getMailSenderEmail());
		assertEquals(MAIL_SENDER_NAME, form.getMailSenderName());
		assertEquals(MAIL_SUBJECT_PREFIX, form.getMailSubjectPrefix());
		assertEquals(MAIL_DEFAULT_LANGUAGE, form.getMailDefaultLanguage());
		assertEquals(BCRYPT_ROUNDS, form.getBcryptRounds());
		assertEquals(PAGE_FOOTER_HTML, form.getPageFooterHtml());
		assertEquals(UPDATE_CHECK_INTERVAL, form.getUpdateCheckInterval());
		assertEquals("macroValue1", form.getMacroSettings().get("macro1").get("key1")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		assertEquals("macroValue2", form.getMacroSettings().get("macro2").get("key2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
