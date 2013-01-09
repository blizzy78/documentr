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
package de.blizzy.documentr.web.account;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openid4java.OpenIDException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.openid.OpenID4JavaConsumer;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.openid.OpenIDConsumerException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.base.Charsets;

import de.blizzy.documentr.access.OpenId;
import de.blizzy.documentr.access.OpenIdNotFoundException;
import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.system.SystemSettingsStore;
import de.blizzy.documentr.web.util.FacadeHostRequestWrapper;
import de.blizzy.documentr.web.util.FacadeHostRequestWrapperFactory;

@Controller
@RequestMapping("/accountOpenId")
public class AccountOpenIdController {
	@Autowired
	private UserStore userStore;
	@Autowired
	private FacadeHostRequestWrapperFactory facadeHostRequestWrapperFactory;
	@Autowired
	private SystemSettingsStore systemSettingsStore;

	@RequestMapping(value="/save", method=RequestMethod.POST)
	@PreAuthorize("isAuthenticated()")
	public String saveOpenId(@RequestParam(required=false) String openId, HttpServletRequest request)
			throws OpenIDException, OpenIDConsumerException {

		try {
			HttpSession session = request.getSession();
			session.removeAttribute("openIdConsumer"); //$NON-NLS-1$
			session.removeAttribute("openId"); //$NON-NLS-1$

			String contextPath = request.getContextPath();
			String documentrHost = systemSettingsStore.getSetting(SystemSettingsStore.DOCUMENTR_HOST);
			OpenIDConsumer consumer = new OpenID4JavaConsumer();
			String returnToUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/accountOpenId/saveFinish") //$NON-NLS-1$
					.build()
					.encode(Charsets.UTF_8.name())
					.toUriString();
			returnToUrl = FacadeHostRequestWrapper.buildFacadeUrl(returnToUrl, contextPath, documentrHost);
			String realm = ServletUriComponentsBuilder.fromCurrentContextPath()
					.path("/").build() //$NON-NLS-1$
					.encode(Charsets.UTF_8.name())
					.toUriString();
			realm = FacadeHostRequestWrapper.buildFacadeUrl(realm, contextPath, documentrHost);
			String url = consumer.beginConsumption(request, openId, returnToUrl, realm);
			session.setAttribute("openIdConsumer", consumer); //$NON-NLS-1$
			session.setAttribute("openId", openId); //$NON-NLS-1$
			return "redirect:" + url; //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@RequestMapping(value="/saveFinish", method=RequestMethod.GET)
	@PreAuthorize("isAuthenticated()")
	public String finishOpenId(HttpServletRequest request, Authentication authentication)
			throws OpenIDConsumerException, IOException {

		HttpSession session = request.getSession();
		OpenIDConsumer consumer = (OpenIDConsumer) session.getAttribute("openIdConsumer"); //$NON-NLS-1$
		session.removeAttribute("openIdConsumer"); //$NON-NLS-1$
		String openId = (String) session.getAttribute("openId"); //$NON-NLS-1$
		session.removeAttribute("openId"); //$NON-NLS-1$

		HttpServletRequest requestWrapper = facadeHostRequestWrapperFactory.create(request);
		OpenIDAuthenticationToken token = consumer.endConsumption(requestWrapper);
		if ((token != null) && (token.getStatus() == OpenIDAuthenticationStatus.SUCCESS)) {
			boolean exists = false;
			try {
				exists = userStore.getUserByOpenId(token.getIdentityUrl()) != null;
			} catch (OpenIdNotFoundException e) {
				// okay
			}

			if (!exists) {
				String loginName = authentication.getName();
				User user = userStore.getUser(loginName);
				OpenId id = new OpenId(openId, token.getIdentityUrl());
				user.addOpenId(id);
				userStore.saveUser(user, user);
			}
		}

		return "redirect:/account/openId"; //$NON-NLS-1$
	}
}
