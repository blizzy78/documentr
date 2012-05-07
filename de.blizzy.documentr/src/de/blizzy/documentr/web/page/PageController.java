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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.NotFoundException;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.pagestore.IPageBranchResolver;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.web.ErrorController;
import de.blizzy.documentr.web.Functions;
import de.blizzy.documentr.web.HtmlSerializerContext;

@Controller
@RequestMapping("/page")
public class PageController {
	@Autowired
	private PageStore pageStore;
	@Autowired
	private GlobalRepositoryManager repoManager;
	@Autowired
	private IPageBranchResolver pageBranchResolver;
	
	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String getPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model, Authentication authentication) throws IOException {

		if ((authentication == null) || !authentication.isAuthenticated()) {
			branchName = pageBranchResolver.resolvePageBranch(projectName, branchName, path);
			if (branchName == null) {
				return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
			}
		}
		
		try {
			path = Util.toRealPagePath(path);
			model.addAttribute("path", path); //$NON-NLS-1$
			Page page = pageStore.getPage(projectName, branchName, path);
			model.addAttribute("title", page.getTitle()); //$NON-NLS-1$
			model.addAttribute("text", page.getText()); //$NON-NLS-1$
			return "/project/branch/page/view"; //$NON-NLS-1$
		} catch (NotFoundException e) {
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
			PageForm form = new PageForm(projectName, branchName, path, page.getParentPagePath(),
					page.getTitle(), page.getText());
			model.addAttribute("pageForm", form); //$NON-NLS-1$
			return "/project/branch/page/edit"; //$NON-NLS-1$
		} catch (NotFoundException e) {
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

		String parentPagePath = Util.toRealPagePath(form.getParentPagePath());
		Page page = Page.fromText(parentPagePath, form.getTitle(), form.getText());
		String path = form.getPath();
		if (StringUtils.isBlank(path)) {
			path = Util.generatePageName(form.getTitle());
		}
		
		if (bindingResult.hasErrors()) {
			return "/project/branch/page/edit"; //$NON-NLS-1$
		}

		String fullPagePath = parentPagePath + "/" + path; //$NON-NLS-1$
		pageStore.savePage(form.getProjectName(), form.getBranchName(), fullPagePath, page);
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
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/json",
			method=RequestMethod.POST)
	@PreAuthorize("permitAll")
	public HttpEntity<String> isPageExistent(@PathVariable String projectName, @PathVariable String branchName,
			@RequestParam String title) throws IOException {

		String name = Util.generatePageName(title);
		String path = name;
		boolean pageExists = false;
		try {
			Page page = pageStore.getPage(projectName, branchName, path);
			pageExists = page != null;
		} catch (NotFoundException e) {
			// okay
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("name", name); //$NON-NLS-1$
		result.put("exists", Boolean.valueOf(pageExists)); //$NON-NLS-1$
		return createJSONResponse(result);
	}

	@RequestMapping(value="/markdownToHTML/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/json",
			method=RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public HttpEntity<String> markdownToHTML(@PathVariable String projectName, @PathVariable String branchName,
			@RequestParam String markdown, @RequestParam(required=false) String pagePath) {
		
		Map<String, String> result = new HashMap<String, String>();
		HtmlSerializerContext context = new HtmlSerializerContext(projectName, branchName, pagePath);
		result.put("html", Functions.markdownToHTML(markdown, context)); //$NON-NLS-1$
		return createJSONResponse(result);
	}

	private HttpEntity<String> createJSONResponse(Object o) {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		String json = gson.toJson(o);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		return new ResponseEntity<String>(json, headers, HttpStatus.OK);
	}
}
