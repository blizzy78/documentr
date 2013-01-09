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
package de.blizzy.documentr.web.page;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.context.request.WebRequest;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.AuthenticationUtil;
import de.blizzy.documentr.access.DocumentrPermissionEvaluator;
import de.blizzy.documentr.access.Permission;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.markdown.IPageRenderer;
import de.blizzy.documentr.markdown.MarkdownProcessor;
import de.blizzy.documentr.page.CommitCherryPickConflictResolve;
import de.blizzy.documentr.page.CommitCherryPickResult;
import de.blizzy.documentr.page.ICherryPicker;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.MergeConflict;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.page.PageNotFoundException;
import de.blizzy.documentr.page.PageTextData;
import de.blizzy.documentr.page.PageUtil;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.util.Util;
import de.blizzy.documentr.web.util.ErrorController;

@Controller
@RequestMapping("/page")
@Slf4j
public class PageController {
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private ICherryPicker cherryPicker;
	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private MarkdownProcessor markdownProcessor;
	@Autowired
	private UserStore userStore;
	@Autowired
	private IPageRenderer pageRenderer;
	@Autowired
	private DocumentrPermissionEvaluator permissionEvaluator;

	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method={ RequestMethod.GET, RequestMethod.HEAD })
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, VIEW)")
	public String getPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		try {
			path = Util.toRealPagePath(path);
			PageMetadata metadata = pageStore.getPageMetadata(projectName, branchName, path);

			long lastEdited = metadata.getLastEdited().getTime();
			long authenticationCreated = AuthenticationUtil.getAuthenticationCreationTime(request.getSession());
			long projectEditTime = PageUtil.getProjectEditTime(projectName);
			long lastModified = Math.max(lastEdited, authenticationCreated);
			if (projectEditTime >= 0) {
				lastModified = Math.max(lastModified, projectEditTime);
			}

			long modifiedSince = request.getDateHeader("If-Modified-Since"); //$NON-NLS-1$
			if ((modifiedSince >= 0) && (lastModified <= modifiedSince)) {
				return ErrorController.notModified();
			}

			response.setDateHeader("Last-Modified", lastModified); //$NON-NLS-1$
			response.setDateHeader("Expires", 0); //$NON-NLS-1$
			response.setHeader("Cache-Control", "must-revalidate, private"); //$NON-NLS-1$ //$NON-NLS-2$

			Page page = pageStore.getPage(projectName, branchName, path, false);
			model.addAttribute("path", path); //$NON-NLS-1$
			model.addAttribute("pageName", //$NON-NLS-1$
					path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path); //$NON-NLS-1$ //$NON-NLS-2$
			model.addAttribute("parentPagePath", page.getParentPagePath()); //$NON-NLS-1$
			model.addAttribute("title", page.getTitle()); //$NON-NLS-1$
			String viewRestrictionRole = page.getViewRestrictionRole();
			model.addAttribute("viewRestrictionRole", //$NON-NLS-1$
					(viewRestrictionRole != null) ? viewRestrictionRole : StringUtils.EMPTY);
			model.addAttribute("commit", metadata.getCommit()); //$NON-NLS-1$
			return "/project/branch/page/view"; //$NON-NLS-1$
		} catch (PageNotFoundException e) {
			return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
		}
	}

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{parentPagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}", method=RequestMethod.GET)
	@PreAuthorize("hasBranchPermission(#projectName, #branchName, EDIT_PAGE)")
	public String createPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String parentPagePath, Model model) {

		PageForm form = new PageForm(projectName, branchName, null,
				Util.toRealPagePath(parentPagePath), null, null, StringUtils.EMPTY, null,
				ArrayUtils.EMPTY_STRING_ARRAY);
		model.addAttribute("pageForm", form); //$NON-NLS-1$
		return "/project/branch/page/edit"; //$NON-NLS-1$
	}

	@RequestMapping(value="/edit/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)")
	public String editPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model, HttpSession session) throws IOException {

		try {
			path = Util.toRealPagePath(path);

			Page page = pageStore.getPage(projectName, branchName, path, true);
			String text = ((PageTextData) page.getData()).getText();
			String viewRestrictionRole = page.getViewRestrictionRole();
			PageMetadata metadata = pageStore.getPageMetadata(projectName, branchName, path);
			String commit = metadata.getCommit();

			MergeConflict conflict = (MergeConflict) session.getAttribute("conflict"); //$NON-NLS-1$
			session.removeAttribute("conflict"); //$NON-NLS-1$
			if (conflict != null) {
				projectName = (String) session.getAttribute("conflict.projectName"); //$NON-NLS-1$
				session.removeAttribute("conflict.projectName"); //$NON-NLS-1$
				branchName = (String) session.getAttribute("conflict.branchName"); //$NON-NLS-1$
				session.removeAttribute("conflict.branchName"); //$NON-NLS-1$
				path = (String) session.getAttribute("conflict.pagePath"); //$NON-NLS-1$
				session.removeAttribute("conflict.pagePath"); //$NON-NLS-1$
				text = conflict.getText();
				commit = conflict.getNewBaseCommit();
			}

			String[] tags = page.getTags().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
			Arrays.sort(tags);
			PageForm form = new PageForm(projectName, branchName, path, page.getParentPagePath(),
					page.getTitle(), text, (viewRestrictionRole != null) ? viewRestrictionRole : StringUtils.EMPTY,
					commit, tags);
			model.addAttribute("pageForm", form); //$NON-NLS-1$
			if (conflict != null) {
				model.addAttribute("mergeConflict", Boolean.TRUE); //$NON-NLS-1$
			}
			return "/project/branch/page/edit"; //$NON-NLS-1$
		} catch (PageNotFoundException e) {
			return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
		}
	}

	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}", method=RequestMethod.POST)
	@PreAuthorize("hasBranchPermission(#form.projectName, #form.branchName, EDIT_PAGE)")
	public String savePage(@ModelAttribute @Valid PageForm form, BindingResult bindingResult,
			Model model, Authentication authentication) throws IOException {

		String projectName = form.getProjectName();
		String branchName = form.getBranchName();
		User user = userStore.getUser(authentication.getName());

		if (!globalRepositoryManager.listProjectBranches(projectName).contains(branchName)) {
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
		Page page = Page.fromText(form.getTitle(), form.getText());
		String path = form.getPath();
		if (StringUtils.isBlank(path)) {
			path = parentPagePath + "/" + Util.simplifyForUrl(form.getTitle()); //$NON-NLS-1$
		}
		page.setTags(Sets.newHashSet(form.getTags()));
		page.setViewRestrictionRole(StringUtils.isNotBlank(form.getViewRestrictionRole()) ?
				form.getViewRestrictionRole() : null);

		Page oldPage = null;
		try {
			oldPage = pageStore.getPage(projectName, branchName, path, true);
		} catch (PageNotFoundException e) {
			// okay
		}
		if ((oldPage == null) || !page.equals(oldPage)) {
			MergeConflict conflict = pageStore.savePage(projectName, branchName, path,
					page, Strings.emptyToNull(form.getCommit()), user);
			if (conflict != null) {
				form.setText(conflict.getText());
				form.setCommit(conflict.getNewBaseCommit());
				model.addAttribute("mergeConflict", Boolean.TRUE); //$NON-NLS-1$
				return "/project/branch/page/edit"; //$NON-NLS-1$
			}
		}

		Integer start = form.getParentPageSplitRangeStart();
		Integer end = form.getParentPageSplitRangeEnd();
		if (StringUtils.isNotBlank(parentPagePath) && (start != null) && (end != null) &&
			permissionEvaluator.hasBranchPermission(authentication, projectName, branchName, Permission.EDIT_PAGE)) {

			log.info("splitting off {}-{} of {}/{}/{}", //$NON-NLS-1$
					start, end, projectName, branchName, parentPagePath);

			Page parentPage = pageStore.getPage(projectName, branchName, parentPagePath, true);
			String text = ((PageTextData) parentPage.getData()).getText();
			end = Math.min(end, text.length());
			text = text.substring(0, start) + text.substring(end);
			parentPage.setData(new PageTextData(text));
			pageStore.savePage(projectName, branchName, parentPagePath, parentPage, null, user);
		}

		return "redirect:/page/" + projectName + "/" + branchName + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Util.toUrlPagePath(path);
	}

	@ModelAttribute
	public PageForm createPageForm(@PathVariable String projectName, @PathVariable String branchName,
			@RequestParam(required=false) String path, @RequestParam(required=false) String parentPagePath,
			@RequestParam(required=false) String title, @RequestParam(required=false) String text,
			@RequestParam(required=false) String viewRestrictionRole,
			@RequestParam(required=false) String commit,
			@RequestParam(required=false) String[] tags) {

		if (tags == null) {
			tags = ArrayUtils.EMPTY_STRING_ARRAY;
		}
		return ((path != null) && (title != null) && (text != null)) ?
				new PageForm(projectName, branchName, path, parentPagePath, title, text,
						StringUtils.isNotBlank(viewRestrictionRole) ? viewRestrictionRole : StringUtils.EMPTY,
						Strings.emptyToNull(commit), tags) :
				null;
	}

	@RequestMapping(value="/generateName/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{parentPagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("hasBranchPermission(#projectName, #branchName, VIEW)")
	public Map<String, Object> generateName(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String parentPagePath, @RequestParam String title) throws IOException {

		String name = Util.simplifyForUrl(title);
		String path = Util.toRealPagePath(parentPagePath) + "/" + name; //$NON-NLS-1$
		boolean pageExists = false;
		try {
			Page page = pageStore.getPage(projectName, branchName, path, false);
			pageExists = page != null;
		} catch (PageNotFoundException e) {
			// okay
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("path", path); //$NON-NLS-1$
		result.put("exists", Boolean.valueOf(pageExists)); //$NON-NLS-1$
		return result;
	}

	@RequestMapping(value="/markdownToHtml/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/json",
			method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	@PreAuthorize("isAuthenticated()")
	public Map<String, String> markdownToHtml(@PathVariable String projectName, @PathVariable String branchName,
			@RequestParam String markdown, @RequestParam(required=false) String pagePath, Authentication authentication,
			HttpServletRequest request) {

		String contextPath = request.getContextPath();
		Map<String, String> result = new HashMap<String, String>();
		String html = markdownProcessor.markdownToHtml(markdown, projectName, branchName, pagePath, authentication,
				contextPath);
		html = markdownProcessor.processNonCacheableMacros(html, projectName, branchName, pagePath, authentication,
				contextPath);
		result.put("html", html); //$NON-NLS-1$
		return result;
	}

	@RequestMapping(value="/copyToBranch/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.POST)
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, VIEW) and " +
			"hasBranchPermission(#projectName, #targetBranchName, EDIT_PAGE)")
	public String copyToBranch(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam String targetBranchName, Authentication authentication)
			throws IOException {

		path = Util.toRealPagePath(path);
		Page page = pageStore.getPage(projectName, branchName, path, true);
		User user = userStore.getUser(authentication.getName());
		pageStore.savePage(projectName, targetBranchName, path, page, null, user);
		return "redirect:/page/edit/" + projectName + "/" + targetBranchName + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				Util.toUrlPagePath(path);
	}

	@RequestMapping(value="/delete/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("hasBranchPermission(#projectName, #branchName, EDIT_PAGE)")
	public String deletePage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Authentication authentication) throws IOException {

		path = Util.toRealPagePath(path);
		User user = userStore.getUser(authentication.getName());
		pageStore.deletePage(projectName, branchName, path, user);
		return "redirect:/page/" + projectName + "/" + branchName + //$NON-NLS-1$ //$NON-NLS-2$
				"/" + DocumentrConstants.HOME_PAGE_NAME; //$NON-NLS-1$
	}

	@RequestMapping(value="/relocate/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.POST)
	@PreAuthorize("hasBranchPermission(#projectName, #branchName, EDIT_PAGE)")
	public String relocatePage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam String newParentPagePath, Authentication authentication)
			throws IOException {

		path = Util.toRealPagePath(path);
		newParentPagePath = Util.toRealPagePath(newParentPagePath);

		User user = userStore.getUser(authentication.getName());
		pageStore.relocatePage(projectName, branchName, path, newParentPagePath, user);
		String pageName = path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path; //$NON-NLS-1$ //$NON-NLS-2$
		return "redirect:/page/" + projectName + "/" + branchName + //$NON-NLS-1$ //$NON-NLS-2$
				"/" + Util.toUrlPagePath(newParentPagePath + "/" + pageName); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@RequestMapping(value="/markdown/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, VIEW)")
	public Map<String, String> getPageMarkdown(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam Set<String> versions) throws IOException {

		try {
			return pageStore.getMarkdown(projectName, branchName, Util.toRealPagePath(path), versions);
		} catch (PageNotFoundException e) {
			return Collections.emptyMap();
		}
	}

	@RequestMapping(value="/markdownInRange/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/" +
			"{rangeStart:[0-9]+},{rangeEnd:[0-9]+}/{commit:[0-9a-fA-F]+}/json",
			method=RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, VIEW)")
	public Map<String, String> getPageMarkdownInRange(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @PathVariable int rangeStart, @PathVariable int rangeEnd,
			@PathVariable String commit) throws IOException {

		Page page = pageStore.getPage(projectName, branchName, Util.toRealPagePath(path), commit, true);
		String markdown = ((PageTextData) page.getData()).getText();
		Map<String, String> result = Maps.newHashMap();
		markdown = markdown.substring(rangeStart, Math.min(rangeEnd, markdown.length()));
		markdown = markdown.replaceAll("[\\r\\n]+$", StringUtils.EMPTY); //$NON-NLS-1$
		result.put("markdown", markdown); //$NON-NLS-1$
		return result;
	}

	@RequestMapping(value="/changes/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated() and hasPagePermission(#projectName, #branchName, #path, VIEW)")
	public String getPageChanges(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model) {

		model.addAttribute("projectName", projectName); //$NON-NLS-1$
		model.addAttribute("branchName", branchName); //$NON-NLS-1$
		path = Util.toRealPagePath(path);
		model.addAttribute("path", path); //$NON-NLS-1$
		return "/project/branch/page/changes"; //$NON-NLS-1$
	}

	@RequestMapping(value="/saveRange/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)")
	public Map<String, Object> savePageRange(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam String markdown, @RequestParam String range,
			@RequestParam String commit, Authentication authentication, HttpServletRequest request) throws IOException {

		path = Util.toRealPagePath(path);

		markdown = markdown.replaceAll("[\\r\\n]+$", StringUtils.EMPTY); //$NON-NLS-1$
		int rangeStart = Integer.parseInt(StringUtils.substringBefore(range, ",")); //$NON-NLS-1$
		int rangeEnd = Integer.parseInt(StringUtils.substringAfter(range, ",")); //$NON-NLS-1$

		Page page = pageStore.getPage(projectName, branchName, path, commit, true);
		String text = ((PageTextData) page.getData()).getText();
		rangeEnd = Math.min(rangeEnd, text.length());

		String oldMarkdown = text.substring(rangeStart, rangeEnd);
		String cleanedOldMarkdown = oldMarkdown.replaceAll("[\\r\\n]+$", StringUtils.EMPTY); //$NON-NLS-1$
		rangeEnd -= oldMarkdown.length() - cleanedOldMarkdown.length();

		String newText = text.substring(0, rangeStart) + markdown + text.substring(Math.min(rangeEnd, text.length()));

		page.setData(new PageTextData(newText));
		User user = userStore.getUser(authentication.getName());
		MergeConflict conflict = pageStore.savePage(projectName, branchName, path, page, commit, user);

		Map<String, Object> result = Maps.newHashMap();
		if (conflict != null) {
			result.put("conflict", Boolean.TRUE); //$NON-NLS-1$
			HttpSession session = request.getSession();
			session.setAttribute("conflict", conflict); //$NON-NLS-1$
			session.setAttribute("conflict.projectName", projectName); //$NON-NLS-1$
			session.setAttribute("conflict.branchName", branchName); //$NON-NLS-1$
			session.setAttribute("conflict.path", path); //$NON-NLS-1$
		} else {
			String newCommit = pageStore.getPageMetadata(projectName, branchName, path).getCommit();
			String contextPath = request.getContextPath();
			String html = pageRenderer.getHtml(projectName, branchName, path, authentication, contextPath);
			html = markdownProcessor.processNonCacheableMacros(html, projectName, branchName, path, authentication,
					contextPath);
			result.put("html", html); //$NON-NLS-1$
			result.put("commit", newCommit); //$NON-NLS-1$
		}
		return result;
	}

	@RequestMapping(value="/restoreVersion/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, EDIT_PAGE)")
	public void restoreVersion(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam String version, Authentication authentication) throws IOException {

		User user = userStore.getUser(authentication.getName());
		pageStore.restorePageVersion(projectName, branchName, Util.toRealPagePath(path), version, user);
	}

	@RequestMapping(value="/cherryPick/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.POST)
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #path, VIEW)")
	public String cherryPick(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @RequestParam String version1, @RequestParam String version2,
			@RequestParam("branch") Set<String> targetBranches, @RequestParam boolean dryRun,
			WebRequest request, Model model, Authentication authentication, Locale locale) throws IOException {

		path = Util.toRealPagePath(path);

		for (String targetBranch : targetBranches) {
			if (!permissionEvaluator.hasPagePermission(
					authentication, projectName, targetBranch, path, Permission.EDIT_PAGE)) {

				return ErrorController.forbidden();
			}
		}

		List<String> commits = cherryPicker.getCommitsList(projectName, branchName, path, version1, version2);
		if (commits.isEmpty()) {
			throw new IllegalArgumentException("no commits to cherry-pick"); //$NON-NLS-1$
		}

		User user = userStore.getUser(authentication.getName());

		Map<String, String[]> params = request.getParameterMap();
		Set<CommitCherryPickConflictResolve> resolves = Sets.newHashSet();
		for (Map.Entry<String, String[]> entry : params.entrySet()) {
			String name = entry.getKey();
			if (name.startsWith("resolveText_")) { //$NON-NLS-1$
				String branchCommit = StringUtils.substringAfter(name, "_"); //$NON-NLS-1$
				String branch = StringUtils.substringBefore(branchCommit, "/"); //$NON-NLS-1$
				String commit = StringUtils.substringAfter(branchCommit, "/"); //$NON-NLS-1$
				String text = entry.getValue()[0];
				CommitCherryPickConflictResolve resolve = new CommitCherryPickConflictResolve(branch, commit, text);
				resolves.add(resolve);
			}
		}

		Map<String, List<CommitCherryPickResult>> results = cherryPicker.cherryPick(
				projectName, branchName, path, commits, targetBranches, resolves, dryRun, user, locale);
		if (results.keySet().size() != targetBranches.size()) {
			throw new IllegalStateException();
		}

		if (!dryRun) {
			boolean allOk = true;
			loop: for (List<CommitCherryPickResult> branchResults : results.values()) {
				for (CommitCherryPickResult result : branchResults) {
					if (result.getStatus() != CommitCherryPickResult.Status.OK) {
						allOk = false;
						break loop;
					}
				}
			}

			if (allOk) {
				return "redirect:/page/" + projectName + "/" + branchName + //$NON-NLS-1$ //$NON-NLS-2$
						"/" + Util.toUrlPagePath(path); //$NON-NLS-1$
			}
		}

		model.addAttribute("cherryPickResults", results); //$NON-NLS-1$
		model.addAttribute("version1", version1); //$NON-NLS-1$
		model.addAttribute("version2", version2); //$NON-NLS-1$
		model.addAttribute("resolves", resolves); //$NON-NLS-1$
		return "/project/branch/page/cherryPick"; //$NON-NLS-1$
	}

	@RequestMapping(value="/split/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/" +
			"{rangeStart:[0-9]+},{rangeEnd:[0-9]+}",
			method=RequestMethod.GET)
	@PreAuthorize("hasBranchPermission(#projectName, #branchName, EDIT_PAGE)")
	public String splitPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, @PathVariable int rangeStart, @PathVariable int rangeEnd,
			Model model) throws IOException {

		log.info("splitting off {}-{} of {}/{}/{}", //$NON-NLS-1$
				rangeStart, rangeEnd, projectName, branchName, path);

		path = Util.toRealPagePath(path);
		Page page = pageStore.getPage(projectName, branchName, path, true);
		String text = ((PageTextData) page.getData()).getText();
		rangeEnd = Math.min(rangeEnd, text.length());
		text = text.substring(rangeStart, rangeEnd).trim();
		PageForm form = new PageForm(projectName, branchName, null, path,
				null, text, StringUtils.EMPTY, null, ArrayUtils.EMPTY_STRING_ARRAY);
		form.setParentPageSplitRangeStart(rangeStart);
		form.setParentPageSplitRangeEnd(rangeEnd);
		model.addAttribute("pageForm", form); //$NON-NLS-1$
		return "/project/branch/page/edit"; //$NON-NLS-1$
	}
}
