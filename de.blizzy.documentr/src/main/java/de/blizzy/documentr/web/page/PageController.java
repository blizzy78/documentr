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
package de.blizzy.documentr.web.page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageNotFoundException;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.ErrorController;
import de.blizzy.documentr.web.markdown.MarkdownProcessor;
import de.blizzy.documentr.web.markdown.macro.MacroFactory;

@Controller
@RequestMapping("/page")
public class PageController {
	@Autowired
	private PageStore pageStore;
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private MacroFactory macroFactory;
	
	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String getPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model) throws IOException {

		try {
			path = Util.toRealPagePath(path);
			model.addAttribute("path", path); //$NON-NLS-1$
			Page page = pageStore.getPage(projectName, branchName, path);
			model.addAttribute("title", page.getTitle()); //$NON-NLS-1$
			model.addAttribute("text", page.getText()); //$NON-NLS-1$
			return "/project/branch/page/view"; //$NON-NLS-1$
		} catch (PageNotFoundException e) {
			return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
		}
	}

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{parentPagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String createPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String parentPagePath, Model model) {

		PageForm form = new PageForm(projectName, branchName, StringUtils.EMPTY,
				Util.toRealPagePath(parentPagePath), StringUtils.EMPTY, StringUtils.EMPTY);
		model.addAttribute("pageForm", form); //$NON-NLS-1$
		return "/project/branch/page/edit"; //$NON-NLS-1$
	}
	
	@RequestMapping(value="/edit/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String editPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model) throws IOException {
		
		try {
			path = Util.toRealPagePath(path);
			Page page = pageStore.getPage(projectName, branchName, path);
			if (path.contains("/")) { //$NON-NLS-1$
				path = StringUtils.substringAfterLast(path, "/"); //$NON-NLS-1$
			}
			PageForm form = new PageForm(projectName, branchName,
					path, page.getParentPagePath(),
					page.getTitle(), page.getText());
			model.addAttribute("pageForm", form); //$NON-NLS-1$
			return "/project/branch/page/edit"; //$NON-NLS-1$
		} catch (PageNotFoundException e) {
			return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
		}
	}
	
	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}", method=RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public String savePage(@ModelAttribute @Valid PageForm form, BindingResult bindingResult)
			throws IOException {
		
		if (!repoManager.listProjectBranches(form.getProjectName()).contains(form.getBranchName())) {
			bindingResult.rejectValue("branchName", "page.branch.nonexistent"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (bindingResult.hasErrors()) {
			return "/project/branch/page/edit"; //$NON-NLS-1$
		}

		String parentPagePath = form.getParentPagePath();
		if (StringUtils.isBlank(parentPagePath)) {
			parentPagePath = null;
		}
		parentPagePath = Util.toRealPagePath(parentPagePath);
		Page page = Page.fromText(parentPagePath, form.getTitle(), form.getText());
		String path = form.getPath();
		if (StringUtils.isBlank(path)) {
			path = Util.simplifyForURL(form.getTitle());
		}
		
		if (bindingResult.hasErrors()) {
			return "/project/branch/page/edit"; //$NON-NLS-1$
		}

		String fullPagePath = (parentPagePath != null) ? (parentPagePath + "/" + path) : path; //$NON-NLS-1$
		Page oldPage = null;
		try {
			oldPage = pageStore.getPage(form.getProjectName(), form.getBranchName(), fullPagePath);
		} catch (PageNotFoundException e) {
			// okay
		}
		if ((oldPage == null) || !page.equals(oldPage)) {
			pageStore.savePage(form.getProjectName(), form.getBranchName(), fullPagePath, page);
		}

		return "redirect:/page/" + form.getProjectName() + "/" + form.getBranchName() + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Util.toURLPagePath(fullPagePath);
	}
	
	@ModelAttribute
	public PageForm createPageForm(@PathVariable String projectName, @PathVariable String branchName,
			@RequestParam(required=false) String path, @RequestParam(required=false) String parentPagePath,
			@RequestParam(required=false) String title, @RequestParam(required=false) String text) {
		
		return ((path != null) && (title != null) && (text != null)) ?
				new PageForm(projectName, branchName, path, parentPagePath, title, text) : null;
	}
	
	@RequestMapping(value="/generateName/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{parentPagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("permitAll")
	public Map<String, Object> generateName(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String parentPagePath, @RequestParam String title) throws IOException {

		String name = Util.simplifyForURL(title);
		String path = Util.toRealPagePath(parentPagePath) + "/" + name; //$NON-NLS-1$
		boolean pageExists = false;
		try {
			Page page = pageStore.getPage(projectName, branchName, path);
			pageExists = page != null;
		} catch (PageNotFoundException e) {
			// okay
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("path", path); //$NON-NLS-1$
		result.put("exists", Boolean.valueOf(pageExists)); //$NON-NLS-1$
		return result;
	}

	@RequestMapping(value="/markdownToHTML/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("isAuthenticated()")
	public Map<String, String> markdownToHTML(@PathVariable String projectName, @PathVariable String branchName,
			@RequestParam String markdown, @RequestParam(required=false) String pagePath) {

		MarkdownProcessor proc = new MarkdownProcessor(projectName, branchName, pagePath, macroFactory);
		Map<String, String> result = new HashMap<String, String>();
		result.put("html", proc.markdownToHTML(markdown)); //$NON-NLS-1$
		return result;
	}
}
