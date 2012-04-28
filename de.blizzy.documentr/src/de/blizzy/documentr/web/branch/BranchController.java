package de.blizzy.documentr.web.branch;

import java.io.IOException;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Autowired;
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
import de.blizzy.documentr.repository.RepositoryUtil;

@Controller
@RequestMapping("/branch")
public class BranchController {
	@Autowired
	private GlobalRepositoryManager repoManager;

	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{name:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}", method=RequestMethod.GET)
	public String getBranch(@PathVariable String projectName, @PathVariable String name, Model model) {
		model.addAttribute("projectName", projectName); //$NON-NLS-1$
		model.addAttribute("name", name); //$NON-NLS-1$
		return "/project/branch/view"; //$NON-NLS-1$
	}

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.GET)
	public String createBranch(@PathVariable String projectName, Model model) {
		BranchForm form = new BranchForm(projectName, StringUtils.EMPTY, null);
		model.addAttribute("branchForm", form); //$NON-NLS-1$
		return "/project/branch/edit"; //$NON-NLS-1$
	}
	
	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}", method=RequestMethod.POST)
	public String saveBranch(@ModelAttribute @Valid BranchForm form, BindingResult bindingResult)
			throws IOException, GitAPIException {
		
		if (repoManager.listProjectBranches(form.getProjectName()).contains(form.getName())) {
			bindingResult.rejectValue("name", "branch.name.exists"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (bindingResult.hasErrors()) {
			return "/project/branch/edit"; //$NON-NLS-1$
		}
		
		Repository repo = null;
		try {
			repo = repoManager.createProjectBranchRepository(form.getProjectName(), form.getName(), form.getStartingBranch());
		} finally {
			RepositoryUtil.closeQuietly(repo);
		}
		return "redirect:/branch/" + form.getProjectName() + "/" + form.getName(); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@ModelAttribute
	public BranchForm createBranchForm(@PathVariable String projectName,@RequestParam(required=false) String name,
			@RequestParam(required=false) String startingBranch) {
		
		return (name != null) ? new BranchForm(projectName, name, startingBranch) : null;
	}
}
