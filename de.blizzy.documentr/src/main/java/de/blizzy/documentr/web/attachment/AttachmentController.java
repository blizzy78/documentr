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
package de.blizzy.documentr.web.attachment;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.access.AuthenticationUtil;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.page.IPageStore;
import de.blizzy.documentr.page.Page;
import de.blizzy.documentr.page.PageMetadata;
import de.blizzy.documentr.page.PageNotFoundException;
import de.blizzy.documentr.page.PageUtil;
import de.blizzy.documentr.util.Util;

@Controller
@RequestMapping("/attachment")
@Slf4j
public class AttachmentController {
	@Autowired
	private IPageStore pageStore;
	@Autowired
	private ServletContext servletContext;
	@Autowired
	private UserStore userStore;

	@RequestMapping(value="/list/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated() and hasPagePermission(#projectName, #branchName, #pagePath, VIEW)")
	public String getAttachments(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, Model model) {

		model.addAttribute("projectName", projectName); //$NON-NLS-1$
		model.addAttribute("branchName", branchName); //$NON-NLS-1$
		pagePath = Util.toRealPagePath(pagePath);
		model.addAttribute("pagePath", pagePath); //$NON-NLS-1$
		return "/project/branch/page/attachments"; //$NON-NLS-1$
	}

	@RequestMapping(value="/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/" +
			"{name:.*}", method={ RequestMethod.GET, RequestMethod.HEAD })
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #pagePath, VIEW)")
	public ResponseEntity<byte[]> getAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, @PathVariable String name, @RequestParam(required=false) boolean download,
			HttpServletRequest request) throws IOException {

		try {
			pagePath = Util.toRealPagePath(pagePath);
			PageMetadata metadata = pageStore.getAttachmentMetadata(projectName, branchName, pagePath, name);
			HttpHeaders headers = new HttpHeaders();

			long lastEdited = metadata.getLastEdited().getTime();
			long authenticationCreated = AuthenticationUtil.getAuthenticationCreationTime(request.getSession());
			long lastModified = Math.max(lastEdited, authenticationCreated);
			if (!download) {
				long projectEditTime = PageUtil.getProjectEditTime(projectName);
				if (projectEditTime >= 0) {
					lastModified = Math.max(lastModified, projectEditTime);
				}

				long modifiedSince = request.getDateHeader("If-Modified-Since"); //$NON-NLS-1$
				if ((modifiedSince >= 0) && (lastModified <= modifiedSince)) {
					return new ResponseEntity<byte[]>(headers, HttpStatus.NOT_MODIFIED);
				}
			}

			headers.setLastModified(lastModified);
			headers.setExpires(0);
			headers.setCacheControl("must-revalidate, private"); //$NON-NLS-1$
			if (download) {
				headers.set("Content-Disposition", //$NON-NLS-1$
						"attachment; filename=\"" + name.replace('"', '_') + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}

			Page attachment = pageStore.getAttachment(projectName, branchName, pagePath, name);
			headers.setContentType(MediaType.parseMediaType(attachment.getContentType()));
			return new ResponseEntity<byte[]>(attachment.getData().getData(), headers, HttpStatus.OK);
		} catch (PageNotFoundException e) {
			return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
		}
	}

	@RequestMapping(value="/create/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.GET)
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)")
	public String createAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, Model model) {

		model.addAttribute("projectName", projectName); //$NON-NLS-1$
		model.addAttribute("branchName", branchName); //$NON-NLS-1$
		pagePath = Util.toRealPagePath(pagePath);
		model.addAttribute("pagePath", pagePath); //$NON-NLS-1$
		return "/project/branch/page/editAttachment"; //$NON-NLS-1$
	}

	@RequestMapping(value="/save/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}",
			method=RequestMethod.POST)
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)")
	public String saveAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, @RequestParam MultipartFile file, Authentication authentication)
			throws IOException {

		saveAttachmentInternal(projectName, branchName, pagePath, file, authentication);
		return "redirect:/attachment/list/" + projectName + "/" + branchName + "/" + pagePath; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@RequestMapping(value="/saveViaJson/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/json",
			method=RequestMethod.POST)
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)")
	@ResponseBody
	public Map<String, Object> saveAttachmentViaJson(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, @RequestParam MultipartFile file, Authentication authentication)
					throws IOException {

		log.info("saving attachment via JSON: {}", file.getOriginalFilename()); //$NON-NLS-1$
		saveAttachmentInternal(projectName, branchName, pagePath, file, authentication);

		Map<String, Object> fileResult = Maps.newHashMap();
		fileResult.put("name", file.getOriginalFilename()); //$NON-NLS-1$
		fileResult.put("size", file.getSize()); //$NON-NLS-1$
		fileResult.put("url", StringUtils.EMPTY); //$NON-NLS-1$
		fileResult.put("thumbnail_url", StringUtils.EMPTY); //$NON-NLS-1$
		fileResult.put("delete_url", StringUtils.EMPTY); //$NON-NLS-1$
		fileResult.put("delete_type", StringUtils.EMPTY); //$NON-NLS-1$
		List<Map<String, Object>> filesList = Lists.newArrayList();
		filesList.add(fileResult);
		Map<String, Object> result = Maps.newHashMap();
		result.put("files", filesList); //$NON-NLS-1$
		return result;
	}

	private void saveAttachmentInternal(String projectName, String branchName, String pagePath,
			MultipartFile file, Authentication authentication) throws IOException {

		log.debug("saving attachment: {}", file.getOriginalFilename()); //$NON-NLS-1$

		byte[] data = IOUtils.toByteArray(file.getInputStream());
		String contentType = servletContext.getMimeType(file.getOriginalFilename());
		if (StringUtils.isBlank(contentType)) {
			contentType = DocumentrConstants.DEFAULT_MIME_TYPE;
		}
		Page attachment = Page.fromData(data, contentType);
		pagePath = Util.toRealPagePath(pagePath);
		User user = userStore.getUser(authentication.getName());
		pageStore.saveAttachment(projectName, branchName, pagePath, file.getOriginalFilename(), attachment, user);
	}

	@RequestMapping(value="/delete/{projectName:" + DocumentrConstants.PROJECT_NAME_PATTERN + "}/" +
			"{branchName:" + DocumentrConstants.BRANCH_NAME_PATTERN + "}/" +
			"{pagePath:" + DocumentrConstants.PAGE_PATH_URL_PATTERN + "}/" +
			"{name:.*}",
			method=RequestMethod.GET)
	@PreAuthorize("hasPagePermission(#projectName, #branchName, #pagePath, EDIT_PAGE)")
	public String deleteAttachment(@PathVariable String projectName, @PathVariable String branchName,
			@PathVariable String pagePath, @PathVariable String name, Authentication authentication) throws IOException {

		User user = userStore.getUser(authentication.getName());
		pageStore.deleteAttachment(projectName, branchName, Util.toRealPagePath(pagePath), name, user);
		return "redirect:/attachment/list/" + projectName + "/" + branchName + "/" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Util.toUrlPagePath(pagePath);
	}
}
