package de.blizzy.documentr.web.access;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/users")
public class UsersController {
	@RequestMapping(method=RequestMethod.GET)
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public String getUsers() {
		return "/user/index"; //$NON-NLS-1$
	}
}
