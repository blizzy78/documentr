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
package de.blizzy.documentr.markdown.macro;

import lombok.Getter;

import org.springframework.util.Assert;

public class CompilationMessage {
	public static enum Type {
		WARNING, ERROR;
	}

	@Getter
	private Type type;
	@Getter
	private final int startLine;
	@Getter
	private final int startColumn;
	@Getter
	private final int endLine;
	@Getter
	private final int endColumn;
	@Getter
	private String message;

	public CompilationMessage(Type type, int startLine, int startColumn, int endLine, int endColumn, String message) {
		Assert.notNull(type);
		Assert.isTrue(startLine >= 1);
		Assert.isTrue(startColumn >= 1);
		Assert.isTrue(endLine >= 1);
		Assert.isTrue(endColumn >= 1);
		Assert.hasLength(message);

		this.type = type;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
		this.message = message;
	}
}
