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

import java.io.IOException;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.common.collect.Sets;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.GrantedAuthorityTarget;
import de.blizzy.documentr.access.GrantedAuthorityTarget.Type;
import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.RoleGrantedAuthority;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserNotFoundException;
import de.blizzy.documentr.access.UserStore;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserStore userStore;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@RequestMapping(value="/add", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String addUser(Model model) {
		UserForm form = new UserForm(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
				StringUtils.EMPTY, false, null);
		model.addAttribute("userForm", form); //$NON-NLS-1$
		return "/user/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String saveUser(@ModelAttribute @Valid UserForm form, BindingResult bindingResult,
			Authentication authentication) throws IOException {

		User user = userStore.getUser(authentication.getName());

		if (StringUtils.isNotBlank(form.getOriginalLoginName()) &&
			!form.getOriginalLoginName().equals(UserStore.ANONYMOUS_USER_LOGIN_NAME) &&
			StringUtils.equals(form.getLoginName(), UserStore.ANONYMOUS_USER_LOGIN_NAME)) {

			bindingResult.rejectValue("loginName", "user.loginName.invalid"); //$NON-NLS-1$ //$NON-NLS-2$
			return "/user/edit"; //$NON-NLS-1$
		}

		if (!form.getLoginName().equals(UserStore.ANONYMOUS_USER_LOGIN_NAME)) {
			if (StringUtils.isNotBlank(form.getLoginName()) &&
				(StringUtils.isBlank(form.getOriginalLoginName()) ||
						!form.getLoginName().equals(form.getOriginalLoginName()))) {

				try {
					if (userStore.getUser(form.getLoginName()) != null) {
						bindingResult.rejectValue("loginName", "user.loginName.exists"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (UserNotFoundException e) {
					// okay
				}
			}

			if (StringUtils.isBlank(form.getOriginalLoginName()) &&
				StringUtils.isBlank(form.getPassword1())) {

				bindingResult.rejectValue("password1", "user.password.blank"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (StringUtils.isBlank(form.getOriginalLoginName()) &&
					StringUtils.isBlank(form.getPassword2())) {

				bindingResult.rejectValue("password2", "user.password.blank"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (StringUtils.isBlank(form.getPassword1()) && StringUtils.isNotBlank(form.getPassword2())) {
				bindingResult.rejectValue("password1", "user.password.blank"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (StringUtils.isNotBlank(form.getPassword1()) && StringUtils.isBlank(form.getPassword2())) {
				bindingResult.rejectValue("password2", "user.password.blank"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (StringUtils.isNotBlank(form.getPassword1()) &&
				StringUtils.isNotBlank(form.getPassword2()) &&
				!StringUtils.equals(form.getPassword1(), form.getPassword2())) {

				bindingResult.rejectValue("password1", "user.password.passwordsNotEqual"); //$NON-NLS-1$ //$NON-NLS-2$
				bindingResult.rejectValue("password2", "user.password.passwordsNotEqual"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (bindingResult.hasErrors()) {
				return "/user/edit"; //$NON-NLS-1$
			}

			User existingUser = null;
			String password = null;
			if (StringUtils.isNotBlank(form.getOriginalLoginName())) {
				try {
					existingUser = userStore.getUser(form.getOriginalLoginName());
					password = existingUser.getPassword();
				} catch (UserNotFoundException e) {
					// okay
				}
			}

			if (StringUtils.isNotBlank(form.getPassword1())) {
				password = passwordEncoder.encodePassword(form.getPassword1(), form.getLoginName());
			}

			if (StringUtils.isBlank(password)) {
				bindingResult.rejectValue("password1", "user.password.blank"); //$NON-NLS-1$ //$NON-NLS-2$
				bindingResult.rejectValue("password2", "user.password.blank"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (bindingResult.hasErrors()) {
				return "/user/edit"; //$NON-NLS-1$
			}

			String newUserName = form.getOriginalLoginName();
			if (StringUtils.isBlank(newUserName)) {
				newUserName = form.getLoginName();
			}

			User newUser = new User(newUserName, password, form.getEmail(), form.isDisabled());
			if (existingUser != null) {
				for (OpenId openId : existingUser.getOpenIds()) {
					newUser.addOpenId(openId);
				}
			}
			userStore.saveUser(newUser, user);

			if (StringUtils.isNotBlank(form.getOriginalLoginName()) &&
				!StringUtils.equals(form.getLoginName(), form.getOriginalLoginName())) {

				userStore.renameUser(form.getOriginalLoginName(), form.getLoginName(), user);
			}
		}

		String[] authorityStrs = StringUtils.defaultString(form.getAuthorities()).split("\\|"); //$NON-NLS-1$
		Set<RoleGrantedAuthority> authorities = Sets.newHashSet();
		for (String authorityStr : authorityStrs) {
			if (StringUtils.isNotBlank(authorityStr)) {
				String[] parts = authorityStr.split(":"); //$NON-NLS-1$
				Assert.isTrue(parts.length == 3);
				Type type = Type.valueOf(parts[0]);
				String targetId = parts[1];
				String roleName = parts[2];
				authorities.add(new RoleGrantedAuthority(new GrantedAuthorityTarget(targetId, type), roleName));
			}
		}
		userStore.saveUserAuthorities(form.getLoginName(), authorities, user);

		return "redirect:/users"; //$NON-NLS-1$
	}

	@RequestMapping(value="/edit/{loginName:" + DocumentrConstants.USER_LOGIN_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(ADMIN)")
	public String editUser(@PathVariable String loginName, Model model) throws IOException {
		User user;
		if (loginName.equals(UserStore.ANONYMOUS_USER_LOGIN_NAME)) {
			user = new User(UserStore.ANONYMOUS_USER_LOGIN_NAME, null, "anonymous@example.com", false); //$NON-NLS-1$
		} else {
			user = userStore.getUser(loginName);
		}
		UserForm form = new UserForm(loginName, loginName, StringUtils.EMPTY, StringUtils.EMPTY,
				user.getEmail(), user.isDisabled(), null);
		model.addAttribute("userForm", form); //$NON-NLS-1$
		return "/user/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/delete/{loginName:" + DocumentrConstants.USER_LOGIN_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(ADMIN) and !isAdmin(#loginName)")
	public String deleteUser(@PathVariable String loginName, Authentication authentication) throws IOException {
		User user = userStore.getUser(authentication.getName());
		userStore.deleteUser(loginName, user);
		return "redirect:/users"; //$NON-NLS-1$
	}

	@ModelAttribute
	public UserForm createUserForm(@RequestParam(required=false) String loginName,
			@RequestParam(required=false) String originalLoginName,
			@RequestParam(required=false) String password1, @RequestParam(required=false) String password2,
			@RequestParam(required=false) String email, @RequestParam(required=false) boolean disabled,
			@RequestParam(required=false) String authorities) {

		return (loginName != null) ?
				new UserForm(loginName, originalLoginName, password1, password2, email, disabled, authorities) :
				null;
	}
}
