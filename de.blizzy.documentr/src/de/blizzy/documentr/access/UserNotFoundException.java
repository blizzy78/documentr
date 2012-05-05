package de.blizzy.documentr.access;

import de.blizzy.documentr.NotFoundException;

public class UserNotFoundException extends NotFoundException {
	private String loginName;

	UserNotFoundException(String loginName) {
		this.loginName = loginName;
	}
	
	public String getLoginName() {
		return loginName;
	}
}
