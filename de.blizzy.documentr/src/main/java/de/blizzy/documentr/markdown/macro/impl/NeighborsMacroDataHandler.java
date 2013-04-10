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
package de.blizzy.documentr.markdown.macro.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;

import de.blizzy.documentr.data.AbstractDataHandler;
import de.blizzy.documentr.page.pagetree.AbstractTreeNode;
import de.blizzy.documentr.page.pagetree.PageTreeNodesProvider;
import de.blizzy.documentr.util.Util;

public class NeighborsMacroDataHandler extends AbstractDataHandler {
	@Override
	public List<AbstractTreeNode> getData(String request, Map<String, String[]> parameterMap, Authentication authentication)
			throws InvocationTargetException {

		try {
			if (request.equals("pageChildren")) { //$NON-NLS-1$
				PageTreeNodesProvider nodesProvider = getBeanFactory().getBean(PageTreeNodesProvider.class);
				String projectName = parameterMap.get("project")[0]; //$NON-NLS-1$
				String branchName = parameterMap.get("branch")[0]; //$NON-NLS-1$
				String pagePath = Util.toRealPagePath(parameterMap.get("path")[0]); //$NON-NLS-1$
				return nodesProvider.getPageChildren(projectName, branchName, pagePath, Collections.<String>emptySet(),
						true, false, authentication);
			} else {
				return Collections.emptyList();
			}
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		}
	}
}
