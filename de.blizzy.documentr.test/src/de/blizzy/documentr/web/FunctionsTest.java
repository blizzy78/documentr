package de.blizzy.documentr.web;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class FunctionsTest {
	@Test
	public void join() {
		assertEquals("1, 2, 3", Functions.join( //$NON-NLS-1$
				new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) }, ", ")); //$NON-NLS-1$
		assertEquals("1, 2, 3", Functions.join(Arrays.asList( //$NON-NLS-1$
				new Integer[] { Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3) }), ", ")); //$NON-NLS-1$
		assertEquals("123", Functions.join(Integer.valueOf(123), ", ")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
