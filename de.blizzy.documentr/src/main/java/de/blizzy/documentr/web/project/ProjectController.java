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
package de.blizzy.documentr.web.project;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.repository.IGlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.util.Util;

@Controller
@RequestMapping("/project")
public class ProjectController {
	@Autowired
	private IGlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private UserStore userStore;

	@RequestMapping(value="/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasProjectPermission(#name, VIEW)")
	public String getProject(@PathVariable String name, Model model) {
		model.addAttribute("name", name); //$NON-NLS-1$
		model.addAttribute("originalName", name); //$NON-NLS-1$
		return "/project/view"; //$NON-NLS-1$
	}

	@RequestMapping(value="/create", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(EDIT_PROJECT)")
	public String createProject(Model model) {
		ProjectForm form = new ProjectForm(null, null);
		model.addAttribute("projectForm", form); //$NON-NLS-1$
		return "/project/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/edit/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasProjectPermission(#name, EDIT_PROJECT)")
	public String editProject(@PathVariable String name, Model model) {
		ProjectForm form = new ProjectForm(name, name);
		model.addAttribute("projectForm", form); //$NON-NLS-1$
		return "/project/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("projectExists(#form.name) ? " +
			"hasProjectPermission(#form.name, EDIT_PROJECT) : " +
			"hasApplicationPermission(EDIT_PROJECT)")
	public String saveProject(@ModelAttribute @Valid ProjectForm form, BindingResult bindingResult,
			Authentication authentication) throws IOException, GitAPIException {

		String name = form.getName();
		String originalName = form.getOriginalName();
		List<String> projects = globalRepositoryManager.listProjects();
		if (StringUtils.isNotBlank(originalName)) {
			if (!projects.contains(originalName)) {
				bindingResult.rejectValue("originalName", "project.name.nonexistent"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (!StringUtils.equals(name, originalName) &&
				projects.contains(name)) {

				bindingResult.rejectValue("name", "project.name.exists"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			if (projects.contains(name)) {
				bindingResult.rejectValue("name", "project.name.exists"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		if (bindingResult.hasErrors()) {
			return "/project/edit"; //$NON-NLS-1$
		}

		User user = userStore.getUser(authentication.getName());

		if (StringUtils.isNotBlank(originalName) &&
			!StringUtils.equals(name, originalName)) {

			globalRepositoryManager.renameProject(originalName, name, user);
		} else if (StringUtils.isBlank(originalName)) {
			ILockedRepository repo = null;
			try {
				repo = globalRepositoryManager.createProjectCentralRepository(name, user);
			} finally {
				Util.closeQuietly(repo);
			}
		}
		return "redirect:/project/" + name; //$NON-NLS-1$
	}

	@RequestMapping(value="/importSample/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/json", method=RequestMethod.GET)
	@PreAuthorize("hasProjectPermission(#name, ADMIN)")
	@ResponseBody
	public Map<String, Object> importSampleContents(@PathVariable String name) throws IOException, GitAPIException {
		globalRepositoryManager.importSampleContents(name);
		return Collections.emptyMap();
	}

	@RequestMapping(value="/delete/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(EDIT_PROJECT)")
	public String deleteProject(@PathVariable String name, Authentication authentication) throws IOException {
		User user = userStore.getUser(authentication.getName());
		globalRepositoryManager.deleteProject(name, user);
		return "redirect:/projects"; //$NON-NLS-1$
	}

	@ModelAttribute
	public ProjectForm createProjectForm(@RequestParam(required=false) String name,
			@RequestParam(required=false) String originalName) {
		return (name != null) ? new ProjectForm(name, originalName) : null;
	}
}
