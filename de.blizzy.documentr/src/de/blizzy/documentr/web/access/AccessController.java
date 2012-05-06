package de.blizzy.documentr.web.access;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/access")
public class AccessController {
	@RequestMapping(value="/login", method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String login() {
		return "/login"; //$NON-NLS-1$
	}
}
