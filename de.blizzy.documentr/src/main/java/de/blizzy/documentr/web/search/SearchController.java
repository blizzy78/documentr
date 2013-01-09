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
package de.blizzy.documentr.web.search;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.blizzy.documentr.access.DocumentrAnonymousAuthenticationFactory;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.search.PageIndex;
import de.blizzy.documentr.search.SearchResult;
import de.blizzy.documentr.web.util.ErrorController;

@Controller
@RequestMapping("/search")
@Slf4j
public class SearchController {
	@Autowired
	private PageIndex pageIndex;
	@Autowired
	private DocumentrAnonymousAuthenticationFactory authenticationFactory;

	@RequestMapping(value="/page", method=RequestMethod.GET)
	@PreAuthorize("permitAll")
	public String findPages(@RequestParam("q") String searchText, @RequestParam(value="p", required=false) Integer page,
			Authentication authentication, Model model) throws IOException {

		if (page == null) {
			page = 1;
		}

		// TODO: why can authentication be null here?
		if (authentication == null) {
			authentication = authenticationFactory.create(UserStore.ANONYMOUS_USER_LOGIN_NAME);
		}

		try {
			SearchResult result = pageIndex.findPages(searchText, page, authentication);
			model.addAttribute("searchText", searchText); //$NON-NLS-1$
			model.addAttribute("searchResult", result); //$NON-NLS-1$
			model.addAttribute("page", page); //$NON-NLS-1$
		} catch (ParseException e) {
			log.warn(StringUtils.EMPTY, e);
		} catch (TimeoutException e) {
			return ErrorController.timeout();
		}
		return "/search/result"; //$NON-NLS-1$
	}

	@RequestMapping(value="/tags/json", method=RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("permitAll")
	public Set<String> getAllTags(Authentication authentication) throws IOException, TimeoutException {
		// TODO: why can authentication be null here?
		if (authentication == null) {
			authentication = authenticationFactory.create(UserStore.ANONYMOUS_USER_LOGIN_NAME);
		}

		return pageIndex.getAllTags(authentication);
	}
}
