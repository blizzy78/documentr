package de.blizzy.documentr.web.access;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/access")
public class AccessController {
	@RequestMapping("/login")
	@PreAuthorize("permitAll")
	public String login() {
		return "/login"; //$NON-NLS-1$
	}
}
