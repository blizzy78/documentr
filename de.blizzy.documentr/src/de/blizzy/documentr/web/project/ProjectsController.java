package de.blizzy.documentr.web.project;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/projects")
public class ProjectsController {
	@RequestMapping(method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String getProjects() {
		return "/project/index"; //$NON-NLS-1$
	}
}
