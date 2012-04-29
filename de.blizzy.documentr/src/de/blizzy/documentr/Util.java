package de.blizzy.documentr;

public final class Util {
	private Util() {}

	public static String toRealPagePath(String pagePath) {
		return pagePath.replace(',', '/');
	}

	public static String toURLPagePath(String pagePath) {
		return pagePath.replace('/', ',');
	}
}
