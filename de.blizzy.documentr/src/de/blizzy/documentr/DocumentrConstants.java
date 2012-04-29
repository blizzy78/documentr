package de.blizzy.documentr;

import org.eclipse.jgit.lib.Constants;

public final class DocumentrConstants {
	public static final String PROJECT_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$
	public static final String BRANCH_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$
	private static final String PAGE_NAME_VALID_CHARS_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$
	private static final String PAGE_PATH_REAL_PATTERN =
			PAGE_NAME_VALID_CHARS_PATTERN + "(?:/" + PAGE_NAME_VALID_CHARS_PATTERN + ")*"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String PAGE_PATH_URL_PATTERN =
			PAGE_NAME_VALID_CHARS_PATTERN + "(?:," + PAGE_NAME_VALID_CHARS_PATTERN + ")*"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String PAGE_PATH_PATTERN =
			"(?:" + PAGE_PATH_REAL_PATTERN + "|" + PAGE_PATH_URL_PATTERN + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final String PROJECT_NAMES_BLACKLIST_PATTERN = "(?:create|save|_.*)"; //$NON-NLS-1$
	public static final String BRANCH_NAMES_BLACKLIST_PATTERN =
			"(?:create|save|" + Constants.MASTER + "|_.*)"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String PAGE_PATHS_BLACKLIST_PATTERN = "_.*"; //$NON-NLS-1$
	
	private DocumentrConstants() {}
}
