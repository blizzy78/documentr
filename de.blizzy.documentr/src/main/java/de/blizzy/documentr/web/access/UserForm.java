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
package de.blizzy.documentr.web.access;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

public class UserForm {
	@NotNull(message="{user.loginName.blank}")
	@NotEmpty(message="{user.loginName.blank}")
	@NotBlank(message="{user.loginName.blank}")
	@ValidLoginName
	private String loginName;
	private String password1;
	private String password2;
	private boolean disabled;
	private boolean admin;

	public UserForm(String loginName, String password1, String password2, boolean disabled, boolean admin) {
		this.loginName = loginName;
		this.password1 = password1;
		this.password2 = password2;
		this.disabled = disabled;
		this.admin = admin;
	}
	
	public String getLoginName() {
		return loginName;
	}
	
	public String getPassword1() {
		return password1;
	}
	
	public String getPassword2() {
		return password2;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	public boolean isAdmin() {
		return admin;
	}
}
