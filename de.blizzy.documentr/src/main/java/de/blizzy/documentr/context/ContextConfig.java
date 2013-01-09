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
package de.blizzy.documentr.context;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;

import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.DiskStoreConfiguration;
import net.sf.ehcache.config.MemoryUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.openid.OpenIDAuthenticationProvider;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import de.blizzy.documentr.DocumentrConstants;
import de.blizzy.documentr.Settings;
import de.blizzy.documentr.access.OpenIdUserDetailsService;
import de.blizzy.documentr.web.access.DocumentrOpenIdAuthenticationFilter;

/** Spring application context configuration. */
@Configuration
@EnableWebMvc
@EnableScheduling
@ComponentScan("de.blizzy.documentr")
@ImportResource({ "classpath:/applicationContext-security.xml", "classpath:/applicationContext-cache.xml" })
public class ContextConfig extends WebMvcConfigurerAdapter implements SchedulingConfigurer {
	@Bean
	public ViewResolver viewResolver() {
		UrlBasedViewResolver resolver = new UrlBasedViewResolver();
		resolver.setViewClass(JstlView.class);
		resolver.setPrefix("/WEB-INF/view"); //$NON-NLS-1$
		resolver.setSuffix(".jsp"); //$NON-NLS-1$
		return resolver;
	}

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setFallbackToSystemLocale(false);
		messageSource.setCacheSeconds(10);
		messageSource.setBasenames(new String[] {
				"classpath:documentr_messages", "classpath:ValidationMessages" //$NON-NLS-1$ //$NON-NLS-2$
		});
		return messageSource;
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}

	@Bean(destroyMethod="shutdown")
	@SuppressWarnings("deprecation")
	public net.sf.ehcache.CacheManager ehCacheManager(Settings settings) throws IOException {
		File cacheDir = new File(settings.getDocumentrDataDir(), DocumentrConstants.CACHE_DIR_NAME);
		FileUtils.forceMkdir(cacheDir);

		net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.newInstance(
				new net.sf.ehcache.config.Configuration()
					.name("Ehcache") //$NON-NLS-1$
					.updateCheck(false)
					.diskStore(new DiskStoreConfiguration()
							.path(cacheDir.getAbsolutePath())));
		ehCacheManager.addCache(new Cache(new CacheConfiguration()
			.name("page_html") //$NON-NLS-1$
			.overflowToDisk(true)
			.diskPersistent(true)
			.maxEntriesLocalHeap(1000)
			.maxBytesLocalDisk(100, MemoryUnit.MEGABYTES)
			.timeToIdleSeconds(TimeUnit.SECONDS.convert(30, TimeUnit.DAYS))));
		ehCacheManager.addCache(new Cache(new CacheConfiguration()
			.name("page_header_html") //$NON-NLS-1$
			.overflowToDisk(true)
			.diskPersistent(true)
			.maxEntriesLocalHeap(100)
			.maxBytesLocalDisk(10, MemoryUnit.MEGABYTES)
			.timeToIdleSeconds(TimeUnit.SECONDS.convert(30, TimeUnit.DAYS))));
		ehCacheManager.addCache(new Cache(new CacheConfiguration()
			.name("page_metadata") //$NON-NLS-1$
			.overflowToDisk(true)
			.diskPersistent(true)
			.maxEntriesLocalHeap(1000)
			.maxBytesLocalDisk(10, MemoryUnit.MEGABYTES)
			.timeToIdleSeconds(TimeUnit.SECONDS.convert(30, TimeUnit.DAYS))));
		ehCacheManager.addCache(new Cache(new CacheConfiguration()
			.name("page_view_restriction_role") //$NON-NLS-1$
			.overflowToDisk(true)
			.diskPersistent(true)
			.maxEntriesLocalHeap(1000)
			.maxBytesLocalDisk(10, MemoryUnit.MEGABYTES)
			.timeToIdleSeconds(TimeUnit.SECONDS.convert(30, TimeUnit.DAYS))));
		return ehCacheManager;
	}

	@Bean
	public CacheManager cacheManager(net.sf.ehcache.CacheManager ehCacheManager) {
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		cacheManager.setCacheManager(ehCacheManager);

		return cacheManager;
	}

	@Bean
	public Filter openIdAuthFilter(ProviderManager authManager, RememberMeServices rememberMeServices) {
		DocumentrOpenIdAuthenticationFilter filter = new DocumentrOpenIdAuthenticationFilter();
		filter.setAuthenticationManager(authManager);
		AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/access/login/error"); //$NON-NLS-1$
		filter.setAuthenticationFailureHandler(failureHandler);
		filter.setRememberMeServices(rememberMeServices);
		return filter;
	}

	@Bean
	public AuthenticationProvider openIdAuthProvider(OpenIdUserDetailsService userDetailsService) {
		OpenIDAuthenticationProvider authProvider = new OpenIDAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		return authProvider;
	}

	@Bean
	public EventBus eventBus() {
		return new EventBus();
	}

	@Bean(destroyMethod="shutdown")
	public ListeningExecutorService taskExecutor() {
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
			.setNameFormat("Task Executor (%d)").build(); //$NON-NLS-1$
		ExecutorService executorService = Executors.newScheduledThreadPool(
				DocumentrConstants.TASK_EXECUTOR_THREADS, threadFactory);
		return MoreExecutors.listeningDecorator(executorService);
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskExecutor());
	}
}
