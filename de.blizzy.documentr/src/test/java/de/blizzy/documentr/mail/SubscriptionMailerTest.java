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

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageChangedEvent;
import de.blizzy.documentr.subscription.SubscriptionStore;
import de.blizzy.documentr.system.SystemSettingsStore;

public class SubscriptionMailerTest extends AbstractDocumentrTest {
	private static final String PROJECT = "project"; //$NON-NLS-1$
	private static final String BRANCH = "branch"; //$NON-NLS-1$
	private static final String PAGE = "page"; //$NON-NLS-1$
	private static final String SENDER_EMAIL = "senderEmail"; //$NON-NLS-1$
	private static final String SENDER_NAME = "senderName"; //$NON-NLS-1$
	private static final String SUBJECT_PREFIX = "[subjectPrefix]"; //$NON-NLS-1$
	private static final String SUBSCRIBER_EMAIL = "subscriberEmail"; //$NON-NLS-1$
	private static final String PAGE_TITLE = "pageTitle"; //$NON-NLS-1$
	private static final String PAGE_TEXT = "pageText"; //$NON-NLS-1$
	private static final String MAIL_SUBJECT = "mailSubject"; //$NON-NLS-1$
	private static final String DOCUMENTR_HOST = "http://documentr.org:1234"; //$NON-NLS-1$
	private static final String MAIL_TEXT = "mailText"; //$NON-NLS-1$
	private static final Locale LOCALE = new Locale(Locale.ENGLISH.getLanguage());

	@Mock
	private JavaMailSender sender;
	@Mock
	private MailSenderFactory mailSenderFactory;
	@Mock
	private MimeMessage message;
	@Mock
	private SystemSettingsStore systemSettingsStore;
	@Mock
	private SubscriptionStore subscriptionStore;
	@Mock
	private IPageStore pageStore;
	@Mock
	private MessageSource messageSource;
	@InjectMocks
	private SubscriptionMailer mailer;

	@Before
	public void setUp() throws IOException {
		Whitebox.setInternalState(mailer, MoreExecutors.sameThreadExecutor());

		Map<String, String> settings = Maps.newHashMap();
		settings.put(SystemSettingsStore.MAIL_SENDER_EMAIL, SENDER_EMAIL);
		settings.put(SystemSettingsStore.MAIL_SENDER_NAME, SENDER_NAME);
		settings.put(SystemSettingsStore.MAIL_SUBJECT_PREFIX, SUBJECT_PREFIX);
		settings.put(SystemSettingsStore.MAIL_DEFAULT_LANGUAGE, LOCALE.getLanguage());
		settings.put(SystemSettingsStore.DOCUMENTR_HOST, DOCUMENTR_HOST);
		when(systemSettingsStore.getSettings()).thenReturn(settings);

		when(subscriptionStore.getSubscriberEmails(PROJECT, BRANCH, PAGE))
			.thenReturn(Collections.singleton(SUBSCRIBER_EMAIL));

		when(pageStore.getPage(PROJECT, BRANCH, PAGE, false)).thenReturn(Page.fromText(PAGE_TITLE, PAGE_TEXT));

		when(messageSource.getMessage("mail.pageChanged.subject", new Object[] { PAGE_TITLE }, LOCALE)) //$NON-NLS-1$
			.thenReturn(MAIL_SUBJECT);
		when(messageSource.getMessage("mail.pageChanged.text", new Object[] { //$NON-NLS-1$
				PAGE_TITLE,
				DOCUMENTR_HOST + "/page/" + PROJECT + "/" + BRANCH + "/" + PAGE, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				DOCUMENTR_HOST + "/page/" + PROJECT + "/" + BRANCH + "/" + PAGE + "#changes" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}, LOCALE))
			.thenReturn(MAIL_TEXT);

		when(mailSenderFactory.createSender()).thenReturn(sender);

		when(sender.createMimeMessage()).thenReturn(message);
	}

	@Test
	public void pageChanged() throws IOException, MessagingException {
		mailer.pageChanged(new PageChangedEvent(PROJECT, BRANCH, PAGE));

		verify(message).setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME, Charsets.UTF_8.name()));
		verify(message).setRecipient(RecipientType.TO, new InternetAddress(SUBSCRIBER_EMAIL));
		verify(message).setSubject(SUBJECT_PREFIX + " " + MAIL_SUBJECT, Charsets.UTF_8.name()); //$NON-NLS-1$
		verify(message).setText(MAIL_TEXT, Charsets.UTF_8.name());
		verify(sender).send(message);
	}
}
