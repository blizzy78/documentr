package de.blizzy.documentr.web.attachment;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.repository.Page;
import de.blizzy.documentr.repository.PageStore;

@Controller
@RequestMapping("/attachment")
public class AttachmentController {
	@Autowired
	private PageStore pageStore;
	
	@RequestMapping(value="/list/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	public String getPage(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, Model model) {

		model.addAttribute("projectName", projectName); //$NON-NLS-1$
		model.addAttribute("branchName", branchName); //$NON-NLS-1$
		pagePath = Util.toRealPagePath(pagePath);
		model.addAttribute("pagePath", pagePath); //$NON-NLS-1$
		return "/project/branch/page/attachments"; //$NON-NLS-1$
	}

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	public String createAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, Model model) {
		
		model.addAttribute("projectName", projectName); //$NON-NLS-1$
		model.addAttribute("branchName", branchName); //$NON-NLS-1$
		pagePath = Util.toRealPagePath(pagePath);
		model.addAttribute("pagePath", pagePath); //$NON-NLS-1$
		return "/project/branch/page/editAttachment"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.POST)
	public String saveAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, @RequestParam MultipartFile file) throws IOException {

		byte[] data = IOUtils.toByteArray(file.getInputStream());
		Page attachment = Page.fromData(data, "application/octet-stream"); //$NON-NLS-1$
		pagePath = Util.toRealPagePath(pagePath);
		pageStore.saveAttachment(projectName, branchName, pagePath, file.getOriginalFilename(), attachment);
		
		return null;
	}
}
