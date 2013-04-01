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
package de.blizzy.documentr.web.account;

import java.io.IOException;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;

@Controller
@RequestMapping("/account")
public class AccountController {
	@Autowired
	private UserStore userStore;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@RequestMapping(value="/myAccount", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String getMyAccount(Model model) {
		AccountForm form = new AccountForm(null, null, null);
		model.addAttribute("accountForm", form); //$NON-NLS-1$
		return "/account/index"; //$NON-NLS-1$
	}

	@RequestMapping(value="/openId", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String getMyOpenIds() {
		return "/account/openId"; //$NON-NLS-1$
	}

	@RequestMapping(value="/removeOpenId", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String removeOpenId(@RequestParam String openId, Authentication authentication) throws IOException {
		String loginName = authentication.getName();
		User user = userStore.getUser(loginName);
		user.removeOpenId(openId);
		userStore.saveUser(user, user);
		return "redirect:/account/openId"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public String saveMyAccount(@ModelAttribute @Valid AccountForm form, BindingResult bindingResult, Model model,
			Authentication authentication) throws IOException {

		if (StringUtils.isNotBlank(form.getNewPassword1()) || StringUtils.isNotBlank(form.getNewPassword2())) {
			User user = userStore.getUser(authentication.getName());
			if (StringUtils.isBlank(form.getPassword())) {
				bindingResult.rejectValue("password", "user.password.blank"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (!passwordEncoder.isPasswordValid(user.getPassword(), form.getPassword(), user.getLoginName())) {
				bindingResult.rejectValue("password", "user.password.wrong"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!StringUtils.equals(form.getNewPassword1(), form.getNewPassword2())) {
				bindingResult.rejectValue("newPassword1", "user.password.passwordsNotEqual"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (!bindingResult.hasErrors()) {
				String encodedPassword = passwordEncoder.encodePassword(form.getNewPassword1(), user.getLoginName());
				User newUser = new User(user.getLoginName(), encodedPassword, user.getEmail(), user.isDisabled());
				for (OpenId openId : user.getOpenIds()) {
					newUser.addOpenId(openId);
				}
				userStore.saveUser(newUser, user);
			}
		}

		if (!bindingResult.hasErrors()) {
			model.addAttribute("messageKey", "dataSaved"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return "/account/index"; //$NON-NLS-1$
	}

	@ModelAttribute
	public AccountForm createAccountForm(@RequestParam(required=false) String password,
			@RequestParam(required=false) String newPassword1, @RequestParam(required=false) String newPassword2) {

		return new AccountForm(password, newPassword1, newPassword2);
	}
}
