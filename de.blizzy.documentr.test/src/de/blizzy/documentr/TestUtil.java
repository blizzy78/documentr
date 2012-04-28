package de.blizzy.documentr;

import static junit.framework.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public final class TestUtil {
	private static Set<File> tempDirs = new HashSet<>();

	private TestUtil() {}

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				deleteTempDirs();
			}
		});
	}
	
	public static File createTempDir() {
		String tempDirPath = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		assertTrue(StringUtils.isNotBlank(tempDirPath));
		Path tempDir = new File(tempDirPath).toPath();
		try {
			Path dir = Files.createTempDirectory(tempDir, "documentr-tests-"); //$NON-NLS-1$
			File f = dir.toFile();
			tempDirs.add(f);
			return f;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void deleteTempDirs() {
		for (File dir : tempDirs) {
			try {
				FileUtils.forceDelete(dir);
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
