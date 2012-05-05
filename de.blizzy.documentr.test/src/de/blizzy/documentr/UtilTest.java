package de.blizzy.documentr;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilTest {
	@Test
	public void toRealPagePath() {
		assertEquals("x/y/z", Util.toRealPagePath("x,y,z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("x/y/z", Util.toRealPagePath("x/y/z")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void toURLPagePath() {
		assertEquals("x,y,z", Util.toURLPagePath("x/y/z")); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("x,y,z", Util.toURLPagePath("x,y,z")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Test
	public void generatePageName() {
		assertEquals("changes-for-1-0-x-code-name-wheatley-xl", //$NON-NLS-1$
				Util.generatePageName("Changes For 1.0.x: Code Name \"Wheatley\" (XL)")); //$NON-NLS-1$
	}
}
