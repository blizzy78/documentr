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
package de.blizzy.documentr.web.markdown;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.pegdown.ast.Visitor;

public class MacroNodeTest {
	@Test
	public void getMacroName() {
		MacroNode node = new MacroNode("macro", "params"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("macro", node.getMacroName()); //$NON-NLS-1$
	}

	@Test
	public void getParams() {
		MacroNode node = new MacroNode("macro", "params"); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("params", node.getParams()); //$NON-NLS-1$
	}
	
	@Test
	public void visit() {
		MacroNode node = new MacroNode("macro", "params"); //$NON-NLS-1$ //$NON-NLS-2$
		Visitor visitor = mock(Visitor.class);
		node.accept(visitor);
		verify(visitor).visit(same(node));
	}
}
