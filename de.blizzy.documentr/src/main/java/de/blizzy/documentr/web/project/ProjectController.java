/*
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

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
package de.blizzy.documentr.web.project;

import java.io.IOException;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryUtil;

@Controller
@RequestMapping("/project")
public class ProjectController {
	@Autowired
	private GlobalRepositoryManager repoManager;

	@RequestMapping(value="/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String getProject(@PathVariable String name, Model model) {
		model.addAttribute("name", name); //$NON-NLS-1$
		return "/project/view"; //$NON-NLS-1$
	}

	@RequestMapping(value="/create", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String createProject(Model model) {
		ProjectForm form = new ProjectForm(StringUtils.EMPTY);
		model.addAttribute("projectForm", form); //$NON-NLS-1$
		return "/project/edit"; //$NON-NLS-1$
	}
	
	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public String saveProject(@ModelAttribute @Valid ProjectForm form, BindingResult bindingResult)
			throws IOException, GitAPIException {
		
		if (bindingResult.hasErrors()) {
			return "/project/edit"; //$NON-NLS-1$
		}
		
		ILockedRepository repo = null;
		try {
			repo = repoManager.createProjectCentralRepository(form.getName());
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
		return "redirect:/project/" + form.getName(); //$NON-NLS-1$
	}

	@ModelAttribute
	public ProjectForm createProjectForm(@RequestParam(required=false) String name) {
		return (name != null) ? new ProjectForm(name) : null;
	}
}