package de.blizzy.documentr.web.access;

import java.io.IOException;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.blizzy.documentr.DocumentrConstants;
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
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String addUser(Model model) {
		UserForm form = new UserForm(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false, false);
		model.addAttribute("userForm", form); //$NON-NLS-1$
		return "/user/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String saveUser(@ModelAttribute @Valid UserForm form, BindingResult bindingResult) throws IOException {
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
		try {
			existingUser = userStore.getUser(form.getLoginName());
			password = existingUser.getPassword();
		} catch (UserNotFoundException e) {
			// okay
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

		User user = new User(form.getLoginName(), password, form.isDisabled(), form.isAdmin());
		userStore.saveUser(user);
		
		return "redirect:/users"; //$NON-NLS-1$
	}

	@RequestMapping(value="/edit/{loginName:" + DocumentrConstants.USER_LOGIN_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String editUser(@PathVariable String loginName, Model model) throws IOException {
		User user = userStore.getUser(loginName);
		UserForm form = new UserForm(loginName, StringUtils.EMPTY, StringUtils.EMPTY,
				user.isDisabled(), user.isAdmin());
		model.addAttribute("userForm", form); //$NON-NLS-1$
		return "/user/edit"; //$NON-NLS-1$
	}

	@ModelAttribute
	public UserForm createUserForm(@RequestParam(required=false) String loginName,
			@RequestParam(required=false) String password1, @RequestParam(required=false) String password2,
			@RequestParam(required=false) boolean disabled, @RequestParam(required=false) boolean admin) {
		
		return (loginName != null) ? new UserForm(loginName, password1, password2, disabled, admin) : null;
	}
}
