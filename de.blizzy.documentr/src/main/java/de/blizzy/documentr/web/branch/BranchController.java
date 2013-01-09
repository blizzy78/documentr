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
package de.blizzy.documentr.web.branch;

import java.io.IOException;
import java.util.List;

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

import com.google.common.io.Closeables;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;

@Controller
@RequestMapping("/branch")
public class BranchController {
	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private UserStore userStore;

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasProjectPermission(#projectName, EDIT_BRANCH)")
	public String createBranch(@PathVariable String projectName, Model model) {
		BranchForm form = new BranchForm(projectName, null, null);
		model.addAttribute("branchForm", form); //$NON-NLS-1$
		return "/project/branch/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.POST)
	@PreAuthorize("hasBranchPermission(#form.projectName, #form.name, EDIT_BRANCH)")
	public String saveBranch(@ModelAttribute @Valid BranchForm form, BindingResult bindingResult,
			Authentication authentication) throws IOException, GitAPIException {

		List<String> branches = globalRepositoryManager.listProjectBranches(form.getProjectName());
		boolean firstBranch = branches.isEmpty();
		if (branches.contains(form.getName())) {
			bindingResult.rejectValue("name", "branch.name.exists"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (bindingResult.hasErrors()) {
			return "/project/branch/edit"; //$NON-NLS-1$
		}

		ILockedRepository repo = null;
		try {
			repo = globalRepositoryManager.createProjectBranchRepository(form.getProjectName(), form.getName(), form.getStartingBranch());
		} finally {
			Closeables.closeQuietly(repo);
		}

		if (firstBranch) {
			Page page = Page.fromText("Home", StringUtils.EMPTY); //$NON-NLS-1$
			User user = userStore.getUser(authentication.getName());
			pageStore.savePage(form.getProjectName(), form.getName(),
					DocumentrConstants.HOME_PAGE_NAME, page, null, user);
			return "redirect:/page/edit/" + form.getProjectName() + "/" + form.getName() + //$NON-NLS-1$ //$NON-NLS-2$
					"/" + DocumentrConstants.HOME_PAGE_NAME; //$NON-NLS-1$
		}

		return "redirect:/page/" + form.getProjectName() + "/" + form.getName() + //$NON-NLS-1$ //$NON-NLS-2$
				"/" + DocumentrConstants.HOME_PAGE_NAME; //$NON-NLS-1$
	}

	@ModelAttribute
	public BranchForm createBranchForm(@PathVariable String projectName, @RequestParam(required=false) String name,
			@RequestParam(required=false) String startingBranch) {

		return (name != null) ? new BranchForm(projectName, name, startingBranch) : null;
	}
}
