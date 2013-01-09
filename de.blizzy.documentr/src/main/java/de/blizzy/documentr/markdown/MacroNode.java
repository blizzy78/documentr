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
package de.blizzy.documentr.markdown;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

import org.pegdown.ast.Node;
import org.pegdown.ast.SuperNode;

public class MacroNode extends SuperNode {
	@Getter
	private String macroName;
	@Getter
	private String params;

	public MacroNode(String macroName, String params) {
		this(macroName, params, Collections.<Node>emptyList());
	}

	public MacroNode(String macroName, String params, List<Node> children) {
		super(children);

		this.macroName = macroName;
		this.params = params;
	}
}
