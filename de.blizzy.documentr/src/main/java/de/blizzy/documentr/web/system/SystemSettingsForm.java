/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

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

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Getter;

public class SystemSettingsForm {
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
	@Min(4)
	@Max(31)
	private int bcryptRounds;

	public SystemSettingsForm(String mailHostName, int mailHostPort, String mailSenderEmail,
			String mailSenderName, String mailSubjectPrefix, int bcryptRounds) {

		this.mailHostName = mailHostName;
		this.mailHostPort = mailHostPort;
		this.mailSenderEmail = mailSenderEmail;
		this.mailSenderName = mailSenderName;
		this.mailSubjectPrefix = mailSubjectPrefix;
		this.bcryptRounds = bcryptRounds;
	}
}
