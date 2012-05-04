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
import de.blizzy.documentr.Util;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.NotFoundException;
import de.blizzy.documentr.repository.Page;
import de.blizzy.documentr.repository.PageStore;
import de.blizzy.documentr.web.ErrorController;
import de.blizzy.documentr.web.Functions;

@Controller
@RequestMapping("/page")
public class PageController {
	@Autowired
	private PageStore pageStore;
	@Autowired
	private GlobalRepositoryManager repoManager;
	
	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	public String getPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model) throws IOException {

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
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}", method=RequestMethod.GET)
	public String createPage(@PathVariable String projectName, @PathVariable String branchName, Model model) {
		PageForm form = new PageForm(projectName, branchName, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
		model.addAttribute("pageForm", form); //$NON-NLS-1$
		return "/project/branch/page/edit"; //$NON-NLS-1$
	}
	
	@RequestMapping(value="/edit/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	public String editPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path, Model model) throws IOException {
		
		try {
			path = Util.toRealPagePath(path);
			Page page = pageStore.getPage(projectName, branchName, path);
			PageForm form = new PageForm(projectName, branchName, path, page.getTitle(), page.getText());
			model.addAttribute("pageForm", form); //$NON-NLS-1$
			return "/project/branch/page/edit"; //$NON-NLS-1$
		} catch (NotFoundException e) {
			return ErrorController.notFound("page.notFound"); //$NON-NLS-1$
		}
	}
	
	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}", method=RequestMethod.POST)
	public String savePage(@ModelAttribute @Valid PageForm form, BindingResult bindingResult)
			throws IOException {
		
		if (!repoManager.listProjectBranches(form.getProjectName()).contains(form.getBranchName())) {
			bindingResult.rejectValue("branchName", "page.branch.nonexistent"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (bindingResult.hasErrors()) {
			return "/project/branch/page/edit"; //$NON-NLS-1$
		}

		Page page = Page.fromText(form.getTitle(), form.getText());
		pageStore.savePage(form.getProjectName(), form.getBranchName(), Util.toRealPagePath(form.getPath()), page);
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
	
	@RequestMapping(value="/exists/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{path:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.GET)
	public HttpEntity<String> isPageExistent(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String path) throws IOException {

		boolean pageExists = false;
		try {
			path = Util.toRealPagePath(path);
			Page page = pageStore.getPage(projectName, branchName, path);
			pageExists = page != null;
		} catch (NotFoundException e) {
			// okay
		}

		Map<String, Boolean> result = new HashMap<>();
		result.put("exists", Boolean.valueOf(pageExists)); //$NON-NLS-1$
		return createJSONResponse(result);
	}

	@RequestMapping(value="/markdownToHTML/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/json",
			method=RequestMethod.POST)
	public HttpEntity<String> markdownToHTML(@RequestParam String markdown) {
		
		Map<String, String> result = new HashMap<>();
		result.put("html", Functions.markdownToHTML(markdown)); //$NON-NLS-1$
		return createJSONResponse(result);
	}

	private HttpEntity<String> createJSONResponse(Object o) {
		Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
		String json = gson.toJson(o);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json"); //$NON-NLS-1$ //$NON-NLS-2$
		return new ResponseEntity<>(json, headers, HttpStatus.OK);
	}
}
