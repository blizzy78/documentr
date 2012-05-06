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
