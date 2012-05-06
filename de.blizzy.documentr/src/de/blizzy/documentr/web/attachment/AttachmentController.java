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
package de.blizzy.documentr.web.attachment;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Util;
import de.blizzy.documentr.pagestore.Page;
import de.blizzy.documentr.pagestore.PageNotFoundException;
import de.blizzy.documentr.pagestore.PageStore;

@Controller
@RequestMapping("/attachment")
public class AttachmentController {
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream"; //$NON-NLS-1$
	@Autowired
	private PageStore pageStore;
	@Autowired
	private ServletContext servletContext;
	
	@RequestMapping(value="/list/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String getAttachments(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, Model model) {

		model.addAttribute("projectName", projectName); //$NON-NLS-1$
		model.addAttribute("branchName", branchName); //$NON-NLS-1$
		pagePath = Util.toRealPagePath(pagePath);
		model.addAttribute("pagePath", pagePath); //$NON-NLS-1$
		return "/project/branch/page/attachments"; //$NON-NLS-1$
	}

	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/" +
			"{name:.*}", method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public HttpEntity<byte[]> getAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, @PathVariable String name) throws IOException {
		
		try {
			pagePath = Util.toRealPagePath(pagePath);
			Page attachment = pageStore.getAttachment(projectName, branchName, pagePath, name);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", attachment.getContentType()); //$NON-NLS-1$
			return new ResponseEntity<byte[]>(attachment.getData(), headers, HttpStatus.OK);
		} catch (PageNotFoundException e) {
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
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
	@PreAuthorize("isAuthenticated()")
	public String saveAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, @RequestParam MultipartFile file, Model model) throws IOException {

		byte[] data = IOUtils.toByteArray(file.getInputStream());
		String contentType = servletContext.getMimeType(file.getOriginalFilename());
		if (StringUtils.isBlank(contentType)) {
			contentType = DEFAULT_MIME_TYPE;
		}
		Page attachment = Page.fromData(data, contentType);
		pagePath = Util.toRealPagePath(pagePath);
		pageStore.saveAttachment(projectName, branchName, pagePath, file.getOriginalFilename(), attachment);
		
		return getAttachments(projectName, branchName, pagePath, model);
	}
}
