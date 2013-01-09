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
package de.blizzy.documentr;

import java.util.List;

import org.eclipse.jgit.lib.Constants;

import com.google.common.collect.ImmutableList;

/** Defines various constants used throughout documentr. */
public final class DocumentrConstants {
	/** Pattern a project name must match against. */
	public static final String PROJECT_NAME_PATTERN = "[a-zA-Z0-9_\\-]+"; //$NON-NLS-1$

	/** Pattern a branch name must match against. */
	public static final String BRANCH_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$

	/** Pattern a single page path component must match against. */
	private static final String PAGE_NAME_VALID_CHARS_PATTERN = "[a-zA-Z0-9_\\-]+"; //$NON-NLS-1$

	/** Pattern a page path must match against (real format). */
	private static final String PAGE_PATH_REAL_PATTERN =
			PAGE_NAME_VALID_CHARS_PATTERN + "(?:/" + PAGE_NAME_VALID_CHARS_PATTERN + ")*"; //$NON-NLS-1$ //$NON-NLS-2$

	/** Pattern a page path must match against (URL format). */
	public static final String PAGE_PATH_URL_PATTERN =
			PAGE_NAME_VALID_CHARS_PATTERN + "(?:," + PAGE_NAME_VALID_CHARS_PATTERN + ")*"; //$NON-NLS-1$ //$NON-NLS-2$

	/** Pattern a page path must match against (both real and URL format). */
	public static final String PAGE_PATH_PATTERN =
			"(?:" + PAGE_PATH_REAL_PATTERN + "|" + PAGE_PATH_URL_PATTERN + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/** Pattern a user login name must match against. */
	public static final String USER_LOGIN_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$

	/** Pattern a role name must match against. */
	public static final String ROLE_NAME_PATTERN = "[a-zA-Z0-9_\\- ]+"; //$NON-NLS-1$

	/** Blacklist pattern a project name must not match against. */
	public static final String PROJECT_NAMES_BLACKLIST_PATTERN = "(?:create|save|list|importSample|_.*)"; //$NON-NLS-1$

	/** Blacklist pattern a branch name must not match against. */
	public static final String BRANCH_NAMES_BLACKLIST_PATTERN =
			"(?:create|save|" + Constants.MASTER + "|\\..*|_.*)"; //$NON-NLS-1$ //$NON-NLS-2$

	/** Pattern a macro name must match against. */
	public static final String MACRO_NAME_PATTERN = "[a-zA-Z0-9_\\-]+"; //$NON-NLS-1$

	/** Default content type for binary data. */
	public static final String DEFAULT_MIME_TYPE = "application/octet-stream"; //$NON-NLS-1$

	/** Page name and path of a branch's home page. */
	public static final String HOME_PAGE_NAME = "home"; //$NON-NLS-1$

	/** URL of the sample contents repository. */
	public static final String SAMPLE_REPO_URL = "http://sample.documentr.org/sample.git"; //$NON-NLS-1$

	/** Name of the cache data directory. */
	public static final String CACHE_DIR_NAME = "cache"; //$NON-NLS-1$

	/** Number of threads the task executor thread pool uses. */
	public static final int TASK_EXECUTOR_THREADS = 8;

	/** Name of the pages directory inside a repository. */
	public static final String PAGES_DIR_NAME = "pages"; //$NON-NLS-1$

	/** Name of the attachments directory inside a repository. */
	public static final String ATTACHMENTS_DIR_NAME = "attachments"; //$NON-NLS-1$

	/** Suffix of a page/attachment file in a repository. */
	public static final String PAGE_SUFFIX = ".page"; //$NON-NLS-1$

	/** Suffix of a metadata file in a repository. */
	public static final String META_SUFFIX = ".meta"; //$NON-NLS-1$

	/** Number of iterations the bcrypt password encoder must perform. */
	public static final int PASSWORD_ENCODER_BCRYPT_ITERATIONS = 12;

	/** Default imports for Groovy scripts. */
	@SuppressWarnings("nls")
	public static final List<String> GROOVY_DEFAULT_IMPORTS = ImmutableList.of(
			"de.blizzy.documentr.access",
			"de.blizzy.documentr.markdown",
			"de.blizzy.documentr.markdown.macro",
			"de.blizzy.documentr.page",
			"de.blizzy.documentr.system",
			"org.apache.commons.lang3");

	private DocumentrConstants() {}
}
