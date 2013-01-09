package de.blizzy.documentr.system;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

@Component
@Slf4j
public class UpdateChecker {
	private static final String UPDATE_PROPERTIES_URL =
			"http://documentr.org/attachment/documentr/1.x/home,download/latest-version.txt"; //$NON-NLS-1$

	@Autowired
	private SystemSettingsStore systemSettingsStore;
	@Getter
	private boolean updateAvailable;
	@Getter
	private String latestVersion;
	private long lastCheckTime = -1;

	@Scheduled(fixedRate=24 * 60 * 60 * 1000)
	public void checkForUpdate() {
		if (isCheckNecessary()) {
			log.info("checking for documentr update"); //$NON-NLS-1$

			lastCheckTime = System.currentTimeMillis();

			Version currentVersion = getCurrentVersion();
			if (currentVersion != null) {
				log.debug("current version: {}", currentVersion); //$NON-NLS-1$

				Version latestVersion = getLatestVersionFromServer();
				if (latestVersion != null) {
					log.debug("latest version: {}", latestVersion); //$NON-NLS-1$

					if (latestVersion.compareTo(currentVersion) > 0) {
						updateAvailable = true;
						this.latestVersion = latestVersion.toString();
						log.info("documentr update available: {}", latestVersion); //$NON-NLS-1$
					} else {
						log.info("documentr version is up to date"); //$NON-NLS-1$
					}
				} else {
					log.debug("couldn't determine latest version"); //$NON-NLS-1$
				}
			} else {
				log.debug("couldn't determine current version"); //$NON-NLS-1$
			}
		}
	}

	private boolean isCheckNecessary() {
		String intervalStr = systemSettingsStore.getSetting(SystemSettingsStore.UPDATE_CHECK_INTERVAL);
		boolean result = false;
		if (!intervalStr.equals(SystemSettingsStore.UPDATE_CHECK_INTERVAL_NEVER)) {
			long interval;
			if (intervalStr.equals(SystemSettingsStore.UPDATE_CHECK_INTERVAL_WEEKLY)) {
				interval = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);
			} else {
				interval = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
			}
			long now = System.currentTimeMillis();
			if ((lastCheckTime < 0) || ((now - lastCheckTime) >= interval)) {
				result = true;
			}
		}
		return result;
	}

	private Version getCurrentVersion() {
		InputStreamReader in = null;
		try {
			InputStream stream = getResourceAsStream("/documentr-version.properties"); //$NON-NLS-1$
			if (stream != null) {
				in = new InputStreamReader(stream, Charsets.UTF_8);
				Properties props = new Properties();
				props.load(in);
				String versionStr = (String) props.values().iterator().next();
				if (!versionStr.startsWith("@")) { //$NON-NLS-1$
					return Version.fromString(versionStr);
				}
			}
		} catch (IOException e) {
			log.error(StringUtils.EMPTY, e);
		} finally {
			Closeables.closeQuietly(in);
		}
		return null;
	}

	private InputStream getResourceAsStream(String name) {
		return getClass().getClassLoader().getResourceAsStream(name);
	}

	private Version getLatestVersionFromServer() {
		List<Version> versions = Lists.newArrayList(getLatestVersionsFromServer());
		if (!versions.isEmpty()) {
			Collections.sort(versions);
			Collections.reverse(versions);
			return versions.get(0);
		}
		return null;
	}

	private Set<Version> getLatestVersionsFromServer() {
		String text = loadLatestVersionsFromServer();
		Set<Version> versions = Sets.newHashSet();
		if (StringUtils.isNotBlank(text)) {
			try {
				Properties props = new Properties();
				props.load(new StringReader(text));
				for (Object key : props.keySet()) {
					versions.add(Version.fromString((String) key));
				}
			} catch (IOException e) {
				log.error(StringUtils.EMPTY, e);
			}
		}
		return versions;
	}

	private String loadLatestVersionsFromServer() {
		try {
			return new Downloader().getTextFromUrl(UPDATE_PROPERTIES_URL, Charsets.UTF_8);
		} catch (UnknownHostException e) {
			log.info("couldn't retrieve {}: unknown host", UPDATE_PROPERTIES_URL); //$NON-NLS-1$
		} catch (SocketTimeoutException e) {
			log.info("couldn't retrieve {}: connection timeout", UPDATE_PROPERTIES_URL); //$NON-NLS-1$
		} catch (SocketException e) {
			log.info("couldn't retrieve {}: {}", UPDATE_PROPERTIES_URL, e.getMessage()); //$NON-NLS-1$
		} catch (IOException e) {
			log.warn("error while checking for documentr update", e); //$NON-NLS-1$
		}
		return null;
	}
}
