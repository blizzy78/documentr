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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;

import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageChangedEvent;
import de.blizzy.documentr.subscription.SubscriptionStore;
import de.blizzy.documentr.system.SystemSettingsStore;
import de.blizzy.documentr.util.Util;

@Component
@Slf4j
public class SubscriptionMailer {
	@Autowired
	private SystemSettingsStore systemSettingsStore;
	@Autowired
	private ExecutorService taskExecutor;
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private MessageSource messageSource;
	@Autowired
	private SubscriptionStore subscriptionStore;
	@Autowired
	private MailSenderFactory mailSenderFactory;

	@Subscribe
	public void pageChanged(PageChangedEvent event) {
		final String projectName = event.getProjectName();
		final String branchName = event.getBranchName();
		final String path = event.getPath();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				log.info("page changed, sending notifications: {}/{}/{}", //$NON-NLS-1$
						projectName, branchName, Util.toUrlPagePath(path));
				try {
					sendNotifications(projectName, branchName, path);
				} catch (IOException e) {
					log.error("error sending notifications", e); //$NON-NLS-1$
				} catch (RuntimeException e) {
					log.error("error sending notifications", e); //$NON-NLS-1$
				}
			}
		};
		taskExecutor.submit(runnable);
	}

	private void sendNotifications(String projectName, String branchName, String path)
			throws IOException {

		Map<String, String> settings = systemSettingsStore.getSettings();
		String senderEmail = settings.get(SystemSettingsStore.MAIL_SENDER_EMAIL);
		String senderName = settings.get(SystemSettingsStore.MAIL_SENDER_NAME);
		String subjectPrefix = settings.get(SystemSettingsStore.MAIL_SUBJECT_PREFIX);
		String languageCode = settings.get(SystemSettingsStore.MAIL_DEFAULT_LANGUAGE);
		Locale locale = new Locale(languageCode);
		if (StringUtils.isNotBlank(senderEmail)) {
			JavaMailSender sender = mailSenderFactory.createSender();
			if (sender != null) {
				Set<String> subscriberEmails = subscriptionStore.getSubscriberEmails(projectName, branchName, path);
				if (!subscriberEmails.isEmpty()) {
					Page page = pageStore.getPage(projectName, branchName, path, false);
					String title = page.getTitle();
					String subject = messageSource.getMessage("mail.pageChanged.subject", //$NON-NLS-1$
							new Object[] { title }, locale);
					if (StringUtils.isNotBlank(subjectPrefix)) {
						subject = subjectPrefix.trim() + " " + subject; //$NON-NLS-1$
					}
					String pageUrl = createUrl(settings, "/page/" + projectName + "/" + branchName + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							Util.toUrlPagePath(path));
					String changesUrl = createUrl(settings, "/page/" + projectName + "/" + branchName + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							Util.toUrlPagePath(path) + "#changes"); //$NON-NLS-1$
					String text = messageSource.getMessage("mail.pageChanged.text", //$NON-NLS-1$
							new Object[] { title, pageUrl, changesUrl }, locale);

					sendMail(subject, text, senderEmail, senderName, subscriberEmails, sender);
				} else {
					log.info("no subscribers, not sending mail"); //$NON-NLS-1$
				}
			} else {
				log.info("settings incomplete, not sending mail"); //$NON-NLS-1$
			}
		} else {
			log.info("settings incomplete, not sending mail"); //$NON-NLS-1$
		}
	}

	private void sendMail(String subject, String text, String senderEmail, String senderName,
			Set<String> subscriberEmails, JavaMailSender sender) {

		for (String subscriberEmail : subscriberEmails) {
			try {
				MimeMessage msg = sender.createMimeMessage();
				msg.setFrom(createAddress(senderEmail, senderName));
				msg.setRecipient(RecipientType.TO, createAddress(subscriberEmail, null));
				msg.setSubject(subject, Charsets.UTF_8.name());
				msg.setText(text, Charsets.UTF_8.name());
				sender.send(msg);
			} catch (MessagingException e) {
				log.error("error while sending mail", e); //$NON-NLS-1$
			}
		}
	}

	private Address createAddress(String email, String name) {
		try {
			return new InternetAddress(email, name, Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private String createUrl(Map<String, String> settings, String uri) {
		String documentrHost = settings.get(SystemSettingsStore.DOCUMENTR_HOST);
		return documentrHost + uri;
	}
}
