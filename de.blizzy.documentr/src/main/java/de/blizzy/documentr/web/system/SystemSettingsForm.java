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

import java.util.SortedMap;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.hibernate.validator.constraints.NotBlank;

public class SystemSettingsForm {
	@Getter
	@NotNull
	@NotBlank
	private String documentrHost;
	@Getter
	private String siteNotice;
	@Getter
	private String mailHostName;
	@Getter
	@Min(1)
	@Max(65535)
	private int mailHostPort;
	@Getter
	private String mailSenderEmail;
	@Getter
	private String mailSenderName;
	@Getter
	private String mailSubjectPrefix;
	@Getter
	@NotNull
	@NotBlank
	private String mailDefaultLanguage;
	@Getter
	@Min(4)
	@Max(31)
	private int bcryptRounds;
	@Getter
	private String pageFooterHtml;
	@Getter
	@NotNull
	@NotBlank
	private String updateCheckInterval;
	// macroName -> [key -> value]
	@Getter
	@NotNull
	private SortedMap<String, SortedMap<String, String>> macroSettings;

	public SystemSettingsForm(String documentrHost, String siteNotice, String mailHostName, int mailHostPort,
			String mailSenderEmail, String mailSenderName, String mailSubjectPrefix, String mailDefaultLanguage,
			int bcryptRounds, String pageFooterHtml, String updateCheckInterval,
			SortedMap<String, SortedMap<String, String>> macroSettings) {

		this.documentrHost = documentrHost;
		this.siteNotice = siteNotice;
		this.mailHostName = mailHostName;
		this.mailHostPort = mailHostPort;
		this.mailSenderEmail = mailSenderEmail;
		this.mailSenderName = mailSenderName;
		this.mailSubjectPrefix = mailSubjectPrefix;
		this.mailDefaultLanguage = mailDefaultLanguage;
		this.bcryptRounds = bcryptRounds;
		this.pageFooterHtml = pageFooterHtml;
		this.updateCheckInterval = updateCheckInterval;
		this.macroSettings = macroSettings;
	}
}
