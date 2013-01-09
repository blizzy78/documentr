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
package de.blizzy.documentr.web.access;

import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.hibernate.validator.constraints.NotBlank;

import de.blizzy.documentr.validation.annotation.ValidLoginName;

public class UserForm {
	@NotNull(message="{user.loginName.blank}")
	@NotBlank(message="{user.loginName.blank}")
	@ValidLoginName
	@Getter
	private String loginName;
	@ValidLoginName
	@Getter
	private String originalLoginName;
	@Getter
	private String password1;
	@Getter
	private String password2;
	@NotNull(message="{user.email.blank}")
	@NotBlank(message="{user.email.blank}")
	@Getter
	private String email;
	@Getter
	private boolean disabled;
	@Getter
	private String authorities;

	public UserForm(String loginName, String originalLoginName, String password1, String password2, String email,
			boolean disabled, String authorities) {

		this.loginName = loginName;
		this.originalLoginName = originalLoginName;
		this.password1 = password1;
		this.password2 = password2;
		this.email = email;
		this.disabled = disabled;
		this.authorities = authorities;
	}
}
