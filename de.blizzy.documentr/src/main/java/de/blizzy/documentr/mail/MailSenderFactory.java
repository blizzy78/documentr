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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.system.SystemSettingsStore;

@Component
class MailSenderFactory {
	@Autowired
	private SystemSettingsStore systemSettingsStore;

	JavaMailSender createSender() {
		Map<String, String> settings = systemSettingsStore.getSettings();
		String host = settings.get(SystemSettingsStore.MAIL_HOST_NAME);
		int port = Integer.parseInt(settings.get(SystemSettingsStore.MAIL_HOST_PORT));
		if (StringUtils.isNotBlank(host)) {
			JavaMailSenderImpl sender = new JavaMailSenderImpl();
			sender.setHost(host);
			sender.setPort(port);
			return sender;
		} else {
			return null;
		}
	}
}
