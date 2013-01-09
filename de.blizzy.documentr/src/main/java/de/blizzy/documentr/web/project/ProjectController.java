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

import javax.validation.Valid;

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

import com.google.common.io.Closeables;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;

@Controller
@RequestMapping("/project")
public class ProjectController {
	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private UserStore userStore;

	@RequestMapping(value="/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasProjectPermission(#name, VIEW)")
	public String getProject(@PathVariable String name, Model model) {
		model.addAttribute("name", name); //$NON-NLS-1$
		return "/project/view"; //$NON-NLS-1$
	}

	@RequestMapping(value="/create", method=RequestMethod.GET)
	@PreAuthorize("hasApplicationPermission(EDIT_PROJECT)")
	public String createProject(Model model) {
		ProjectForm form = new ProjectForm(null);
		model.addAttribute("projectForm", form); //$NON-NLS-1$
		return "/project/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("projectExists(#form.name) ? " +
			"hasProjectPermission(#form.name, EDIT_PROJECT) : " +
			"hasApplicationPermission(EDIT_PROJECT)")
	public String saveProject(@ModelAttribute @Valid ProjectForm form, BindingResult bindingResult,
			Authentication authentication) throws IOException, GitAPIException {

		if (bindingResult.hasErrors()) {
			return "/project/edit"; //$NON-NLS-1$
		}

		ILockedRepository repo = null;
		try {
			User user = userStore.getUser(authentication.getName());
			repo = globalRepositoryManager.createProjectCentralRepository(form.getName(), user);
		} finally {
			Closeables.closeQuietly(repo);
		}
		return "redirect:/project/" + form.getName(); //$NON-NLS-1$
	}

	@RequestMapping(value="/importSample/{name:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/json", method=RequestMethod.GET)
	@PreAuthorize("hasProjectPermission(#name, ADMIN)")
	@ResponseBody
	public void importSampleContents(@PathVariable String name) throws IOException, GitAPIException {
		globalRepositoryManager.importSampleContents(name);
	}

	@ModelAttribute
	public ProjectForm createProjectForm(@RequestParam(required=false) String name) {
		return (name != null) ? new ProjectForm(name) : null;
	}
}
