package de.blizzy.documentr.web;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.blizzy.documentr.Util;

public class HtmlSerializerContext {
	private String projectName;
	private String branchName;
	private String pagePath;

	public HtmlSerializerContext(String projectName, String branchName, String pagePath) {
		
		Assert.hasLength(projectName);
		Assert.hasLength(branchName);
		// pagePath can be null for new pages
		
		this.projectName = projectName;
		this.branchName = branchName;
		this.pagePath = pagePath;
	}
	
	String getAttachmentURI(String name) {
		if (StringUtils.isNotBlank(pagePath)) {
			try {
				String pattern = "/attachment/{projectName}/{branchName}/{pagePath}/{name}"; //$NON-NLS-1$
				String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(pattern).build()
					.expand(projectName, branchName, Util.toURLPagePath(pagePath), name)
					.encode("UTF-8").toUriString(); //$NON-NLS-1$
				return uri;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return "#"; //$NON-NLS-1$
	}
}
