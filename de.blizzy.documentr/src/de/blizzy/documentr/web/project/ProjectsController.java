package de.blizzy.documentr.web.project;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/projects")
public class ProjectsController {
	@RequestMapping
	public String getProjects() {
		return "/index"; //$NON-NLS-1$
	}
}
