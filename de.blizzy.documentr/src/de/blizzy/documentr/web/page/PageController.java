package de.blizzy.documentr.web.page;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.repository.NotFoundException;
import de.blizzy.documentr.repository.Page;
import de.blizzy.documentr.repository.PageStore;
import de.blizzy.documentr.web.ErrorController;

@Controller
@RequestMapping("/page")
public class PageController {
	@Autowired
	private PageStore pageStore;
	
	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_PATTERN + "}",
			method=RequestMethod.GET)
	public String getPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model) throws IOException, GitAPIException {

		try {
			model.addAttribute("path", path); //$NON-NLS-1$
			path = Util.toRealPagePath(path);
			Page page = pageStore.getPage(projectName, branchName, path);
			model.addAttribute("title", page.getTitle()); //$NON-NLS-1$
			model.addAttribute("text", page.getText()); //$NON-NLS-1$
			return "/project/branch/page/view"; //$NON-NLS-1$
		} catch (NotFoundException e) {
			return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
		}
	}

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}", method=RequestMethod.GET)
	public String createPage(@PathVariable String projectName, @PathVariable String branchName, Model model) {
		PageForm form = new PageForm(projectName, branchName, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
		model.addAttribute("page", form); //$NON-NLS-1$
		return "/project/branch/page/edit"; //$NON-NLS-1$
	}
	
	@RequestMapping(value="/edit/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_PATTERN + "}",
			method=RequestMethod.GET)
	public String editPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model) throws IOException, GitAPIException {
		
		try {
			path = Util.toRealPagePath(path);
			Page page = pageStore.getPage(projectName, branchName, path);
			PageForm form = new PageForm(projectName, branchName, path, page.getTitle(), page.getText());
			model.addAttribute("page", form); //$NON-NLS-1$
			return "/project/branch/page/edit"; //$NON-NLS-1$
		} catch (NotFoundException e) {
			return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
		}
	}
	
	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}", method=RequestMethod.POST)
	public String savePage(@ModelAttribute PageForm form) throws IOException, GitAPIException {
		
		Page page = Page.fromText(form.getTitle(), form.getText());
		pageStore.savePage(form.getProjectName(), form.getBranchName(), form.getPath(), page);
		return "redirect:/page/" + form.getProjectName() + "/" + form.getBranchName() + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Util.toURLPagePath(form.getPath());
	}
	
	@ModelAttribute
	public PageForm createPageForm(@PathVariable String projectName, @PathVariable String branchName,
			@RequestParam(required=false) String path, @RequestParam(required=false) String title,
			@RequestParam(required=false) String text) {
		
		return ((path != null) && (title != null) && (text != null)) ?
				new PageForm(projectName, branchName, path, title, text) : null;
	}
}
