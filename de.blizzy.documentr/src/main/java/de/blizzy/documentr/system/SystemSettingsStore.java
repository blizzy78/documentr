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
package de.blizzy.documentr.system;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gitective.core.BlobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.blizzy.documentr.access.User;
import de.blizzy.documentr.access.UserStore;
import de.blizzy.documentr.repository.GlobalRepositoryManager;
import de.blizzy.documentr.repository.ILockedRepository;
import de.blizzy.documentr.repository.RepositoryNotFoundException;
import de.blizzy.documentr.repository.RepositoryUtil;

/** Manages storage of system settings. */
@Component
@Slf4j
public class SystemSettingsStore implements Lifecycle {
	public static final String DOCUMENTR_HOST = "documentrHost"; //$NON-NLS-1$
	public static final String SITE_NOTICE = "siteNotice"; //$NON-NLS-1$
	public static final String MAIL_HOST_NAME = "mail.host.name"; //$NON-NLS-1$
	public static final String MAIL_HOST_PORT = "mail.host.port"; //$NON-NLS-1$
	public static final String MAIL_SENDER_EMAIL = "mail.sender.email"; //$NON-NLS-1$
	public static final String MAIL_SENDER_NAME = "mail.sender.name"; //$NON-NLS-1$
	public static final String MAIL_SUBJECT_PREFIX = "mail.subject.prefix"; //$NON-NLS-1$
	public static final String MAIL_DEFAULT_LANGUAGE = "mail.defaultLanguage"; //$NON-NLS-1$
	public static final String BCRYPT_ROUNDS = "bcrypt.rounds"; //$NON-NLS-1$
	public static final String PAGE_FOOTER_HTML = "page.footerHtml"; //$NON-NLS-1$
	public static final String UPDATE_CHECK_INTERVAL = "updateCheck.interval"; //$NON-NLS-1$

	public static final String UPDATE_CHECK_INTERVAL_NEVER = "never"; //$NON-NLS-1$
	public static final String UPDATE_CHECK_INTERVAL_DAILY = "daily"; //$NON-NLS-1$
	public static final String UPDATE_CHECK_INTERVAL_WEEKLY = "weekly"; //$NON-NLS-1$

	private static final String REPOSITORY_NAME = "_system"; //$NON-NLS-1$
	private static final String SETTINGS_FILE_NAME = "settings"; //$NON-NLS-1$
	private static final String MACRO_KEY_PREFIX = "macro."; //$NON-NLS-1$

	@Autowired
	private GlobalRepositoryManager globalRepositoryManager;
	@Autowired
	private UserStore userStore;
	@Autowired
	private EventBus eventBus;
	private Map<String, String> settings;
	private boolean running;

	@PostConstruct
	public void init() throws IOException {
		settings = loadSettings();
		setDefaultSettings();
	}

	@Override
	public void start() {
		running = true;

		try {
			User adminUser = userStore.getUser("admin"); //$NON-NLS-1$
			ILockedRepository repo = globalRepositoryManager.createProjectCentralRepository(REPOSITORY_NAME, false, adminUser);
			Closeables.closeQuietly(repo);

			// repository has just been created, store settings for the first time
			Map<String, String> settingsToStore;
			synchronized (settings) {
				settingsToStore = Maps.newHashMap(settings);
			}
			storeSettings(settingsToStore, adminUser);
		} catch (IllegalStateException e) {
			// okay
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void stop() {
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	private void setDefaultSettings() {
		Map<String, String> defaultSettings = Maps.newHashMap();
		defaultSettings.put(DOCUMENTR_HOST, "http://localhost:8080"); //$NON-NLS-1$
		defaultSettings.put(MAIL_HOST_PORT, "25"); //$NON-NLS-1$
		defaultSettings.put(MAIL_SENDER_NAME, "documentr"); //$NON-NLS-1$
		defaultSettings.put(MAIL_SUBJECT_PREFIX, "[documentr]"); //$NON-NLS-1$
		defaultSettings.put(MAIL_DEFAULT_LANGUAGE, Locale.ENGLISH.getLanguage());
		defaultSettings.put(BCRYPT_ROUNDS, "12"); //$NON-NLS-1$
		defaultSettings.put(UPDATE_CHECK_INTERVAL, UPDATE_CHECK_INTERVAL_WEEKLY);

		for (Map.Entry<String, String> entry : defaultSettings.entrySet()) {
			if (!settings.containsKey(entry.getKey())) {
				settings.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<String, String> getSettings() {
		synchronized (settings) {
			return Collections.unmodifiableMap(Maps.newHashMap(settings));
		}
	}

	public String getSetting(String key) {
		synchronized (settings) {
			return settings.get(key);
		}
	}

	public void saveSettings(Map<String, String> newSettings, User currentUser) throws IOException {
		Map<String,String> settingsToStore;
		synchronized (settings) {
			settings.putAll(newSettings);
			settingsToStore = Maps.newHashMap(settings);
		}
		storeSettings(settingsToStore, currentUser);
		eventBus.post(new SystemSettingsChangedEvent());
	}

	private void storeSettings(Map<String, String> settings, User currentUser) throws IOException {
		log.info("storing system settings"); //$NON-NLS-1$

		ILockedRepository repo = null;
		try {
			repo = getOrCreateRepository(currentUser);
			File workingDir = RepositoryUtil.getWorkingDir(repo.r());
			File file = new File(workingDir, SETTINGS_FILE_NAME);
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			String json = gson.toJson(settings);
			FileUtils.writeStringToFile(file, json, Charsets.UTF_8);
			Git git = Git.wrap(repo.r());
			git.add()
				.addFilepattern(SETTINGS_FILE_NAME)
				.call();
			PersonIdent ident = new PersonIdent(currentUser.getLoginName(), currentUser.getEmail());
			git.commit()
				.setAuthor(ident)
				.setCommitter(ident)
				.setMessage(SETTINGS_FILE_NAME)
				.call();
		} catch (GitAPIException e) {
			throw new IOException(e);
		} finally {
			Closeables.closeQuietly(repo);
		}
	}

	private ILockedRepository getOrCreateRepository(User user) throws IOException, GitAPIException {
		try {
			return globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
		} catch (RepositoryNotFoundException e) {
			return globalRepositoryManager.createProjectCentralRepository(REPOSITORY_NAME, false, user);
		}
	}

	private Map<String, String> loadSettings() throws IOException {
		log.info("loading system settings"); //$NON-NLS-1$

		ILockedRepository repo = null;
		Map<String, String> settingsMap = Maps.newHashMap();
		try {
			repo = globalRepositoryManager.getProjectCentralRepository(REPOSITORY_NAME, false);
			String json = BlobUtils.getHeadContent(repo.r(), SETTINGS_FILE_NAME);
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
			settingsMap = gson.fromJson(json, new TypeToken<Map<String, String>>(){}.getType());
		} catch (RepositoryNotFoundException e) {
			// okay
		} finally {
			Closeables.closeQuietly(repo);
		}
		return settingsMap;
	}

	public void setMacroSetting(String macroName, Map<String, String> newSettings, User currentUser) throws IOException {
		Map<String, String> settings = Maps.newHashMap();
		for (Map.Entry<String, String> entry : newSettings.entrySet()) {
			settings.put(MACRO_KEY_PREFIX + macroName + "." + entry.getKey(), entry.getValue()); //$NON-NLS-1$
		}
		saveSettings(settings, currentUser);
	}

	public String getMacroSetting(String macroName, String key) {
		return getSetting(MACRO_KEY_PREFIX + macroName + "." + key); //$NON-NLS-1$
	}
}
