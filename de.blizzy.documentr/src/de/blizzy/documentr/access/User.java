package de.blizzy.documentr.access;

import java.security.SecureRandom;

import org.springframework.security.core.token.Sha512DigestUtils;

public class User {
	private static SecureRandom random = new SecureRandom();
	
	private String loginName;
	private String password;
	private boolean locked;
	private boolean admin;

	public User(String loginName, String password, boolean locked, boolean admin) {
		this.loginName = loginName;
		this.password = password;
		this.locked = locked;
		this.admin = admin;
	}
	
	public String getLoginName() {
		return loginName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean isLocked() {
		return locked;
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
