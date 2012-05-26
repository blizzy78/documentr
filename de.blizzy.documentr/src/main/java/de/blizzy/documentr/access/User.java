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
package de.blizzy.documentr.access;

import java.security.SecureRandom;

import org.springframework.security.core.token.Sha512DigestUtils;

public class User {
	private static SecureRandom random = new SecureRandom();
	
	private String loginName;
	private String password;
	private String email;
	private boolean disabled;
	private boolean admin;

	public User(String loginName, String password, String email, boolean disabled, boolean admin) {
		this.loginName = loginName;
		this.password = password;
		this.email = email;
		this.disabled = disabled;
		this.admin = admin;
	}
	
	public String getLoginName() {
		return loginName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getEmail() {
		return email;
	}

	public boolean isDisabled() {
		return disabled;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public static String hashPassword(String password, String salt) {
		return Sha512DigestUtils.shaHex(password + salt);
	}
	
	public static String getRandomSalt() {
		return String.valueOf(random.nextLong());
	}
}
