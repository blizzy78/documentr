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
package de.blizzy.documentr.search;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WordPositionTest {
	private static final String WORD = "word"; //$NON-NLS-1$
	private static final int START = 123;
	private static final int END = 234;
	
	private WordPosition position;

	@Before
	public void setUp() {
		position = new WordPosition(WORD, START, END);
	}
	
	@Test
	public void getWord() {
		assertEquals(WORD, position.getWord());
	}
	
	@Test
	public void getStart() {
		assertEquals(START, position.getStart());
	}
	
	@Test
	public void getEnd() {
		assertEquals(END, position.getEnd());
	}
}
