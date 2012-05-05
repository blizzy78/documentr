package de.blizzy.documentr.web.project;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects")
public class ProjectsController {
	@RequestMapping
	@PreAuthorize("permitAll")
	public String getProjects() {
		return "/project/index"; //$NON-NLS-1$
	}
}
