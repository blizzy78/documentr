package de.blizzy.documentr;

public final class DocumentrConstants {
	public static final String PROJECT_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$
	public static final String BRANCH_NAME_PATTERN = "[a-zA-Z0-9_\\.\\-]+"; //$NON-NLS-1$
	public static final String PAGE_PATH_PATTERN = "[a-zA-Z0-9_\\.\\-,]+"; //$NON-NLS-1$

	public static final String[] PROJECT_NAMES_BLACKLIST = { "create", "save" }; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String[] BRANCH_NAMES_BLACKLIST = { "create", "save" }; //$NON-NLS-1$ //$NON-NLS-2$
	
	private DocumentrConstants() {}
}
