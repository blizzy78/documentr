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
package de.blizzy.documentr.markdown.macro;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.common.base.Charsets;

import de.blizzy.documentr.AbstractDocumentrTest;
import de.blizzy.documentr.Settings;

public class GroovyMacroScannerTest extends AbstractDocumentrTest {
	private static final String MACRO_CLASS_NAME = "TestMacro"; //$NON-NLS-1$
	@SuppressWarnings("nls")
	private static final String MACRO =
			"import " + IMacro.class.getPackage().getName() + ".*\n" +
			"class " + MACRO_CLASS_NAME + " implements IMacro {\n" +
				"IMacroDescriptor getDescriptor() { null }\n" +
				"IMacroRunnable createRunnable() { null }\n" +
			"}";
	
	@Rule
	public TemporaryFolder tempDir = new TemporaryFolder();

	@Mock
	private Settings settings;
	@InjectMocks
	private GroovyMacroScanner scanner;
	
	@Before
	public void setUp() throws IOException {
		File dataDir = tempDir.getRoot();
		when(settings.getDocumentrDataDir()).thenReturn(dataDir);
		
		File macrosDir = new File(dataDir, GroovyMacroScanner.MACROS_DIR_NAME);
		File macroFile = new File(macrosDir, "test.groovy"); //$NON-NLS-1$
		FileUtils.writeStringToFile(macroFile, MACRO, Charsets.UTF_8);
	}
	
	@Test
	public void foo() {
		Set<IMacro> macros = scanner.findGroovyMacros();
		IMacro macro = macros.iterator().next();
		assertEquals(MACRO_CLASS_NAME, macro.getClass().getName());
	}
}
