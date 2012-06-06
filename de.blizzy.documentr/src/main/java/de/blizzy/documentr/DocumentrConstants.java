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
package de.blizzy.documentr;

import org.eclipse.jgit.lib.Constants;

public final class DocumentrConstants {
	public static final String PROJECT_NAME_PATTERN = "[a-zA-Z0-9_\\-]+"; //$NON-NLS-1$
	public static final String BRANCH_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$
	private static final String PAGE_NAME_VALID_CHARS_PATTERN = "[a-zA-Z0-9_\\-]+"; //$NON-NLS-1$
	private static final String PAGE_PATH_REAL_PATTERN =
			PAGE_NAME_VALID_CHARS_PATTERN + "(?:/" + PAGE_NAME_VALID_CHARS_PATTERN + ")*"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String PAGE_PATH_URL_PATTERN =
			PAGE_NAME_VALID_CHARS_PATTERN + "(?:," + PAGE_NAME_VALID_CHARS_PATTERN + ")*"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String PAGE_PATH_PATTERN =
			"(?:" + PAGE_PATH_REAL_PATTERN + "|" + PAGE_PATH_URL_PATTERN + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public static final String USER_LOGIN_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$
	public static final String ROLE_NAME_PATTERN = "[a-zA-Z0-9_\\-]+"; //$NON-NLS-1$

	public static final String PROJECT_NAMES_BLACKLIST_PATTERN = "(?:create|save|list|_.*)"; //$NON-NLS-1$
	public static final String BRANCH_NAMES_BLACKLIST_PATTERN =
			"(?:create|save|" + Constants.MASTER + "|\\..*|_.*)"; //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final String DEFAULT_MIME_TYPE = "application/octet-stream"; //$NON-NLS-1$
	
	public static final String ENCODING = "UTF-8"; //$NON-NLS-1$
	
	public static final String ANONYMOUS_AUTH_KEY = String.valueOf((long) Math.random() * Long.MAX_VALUE);
	
	public static final String HOME_PAGE_NAME = "home"; //$NON-NLS-1$

	private DocumentrConstants() {}
}
