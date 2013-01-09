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
package de.blizzy.documentr.util;

import java.util.regex.Pattern;

import org.springframework.util.Assert;

public class Replacement {
	private Pattern pattern;
	private String replaceWith;

	public Replacement(Pattern pattern, String replaceWith) {
		Assert.notNull(pattern);
		Assert.notNull(replaceWith);

		this.pattern = pattern;
		this.replaceWith = replaceWith;
	}

	public Replacement(String pattern, String replaceWith) {
		Assert.hasLength(pattern);
		Assert.notNull(replaceWith);

		this.pattern = Pattern.compile(pattern);
		this.replaceWith = replaceWith;
	}

	public static Replacement dotAllNoCase(String pattern, String replaceWith) {
		Assert.hasLength(pattern);

		Pattern p = Pattern.compile(pattern, Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
		return new Replacement(p, replaceWith);
	}

	public String replaceAll(String s) {
		String result;
		if (s != null) {
			result = pattern.matcher(s).replaceAll(replaceWith);
		} else {
			result = null;
		}
		return result;
	}
}
