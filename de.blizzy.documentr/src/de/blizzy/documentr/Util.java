package de.blizzy.documentr;

import de.blizzy.documentr.web.page.PagePathValidator;


public final class Util {
	private Util() {}

	public static String toRealPagePath(String pagePath) {
		return pagePath.replace(',', '/');
	}

	public static String toURLPagePath(String pagePath) {
		return pagePath.replace('/', ',');
	}

	public static String generatePageName(String title) {
		PagePathValidator validator = new PagePathValidator();
		StringBuilder buf = new StringBuilder();
		int len = title.length();
		for (int i = 0; i < len; i++) {
			char c = title.charAt(i);
			switch (c) {
				case ' ':
				case '.':
				case ',':
				case '/':
				case ':':
				case '(':
				case ')':
				case '[':
				case ']':
				case '<':
				case '>':
					buf.append("-"); //$NON-NLS-1$
					break;
				
				default:
					buf.append(c);
					if (!validator.isValid(buf.toString(), null)) {
						buf.deleteCharAt(buf.length() - 1);
					}
					break;
			}
		}

		String name = buf.toString()
				.replaceAll("--", "-") //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll("^-", "") //$NON-NLS-1$ //$NON-NLS-2$
				.replaceAll("-$", "") //$NON-NLS-1$ //$NON-NLS-2$
				.toLowerCase();
		return name;
	}
}
