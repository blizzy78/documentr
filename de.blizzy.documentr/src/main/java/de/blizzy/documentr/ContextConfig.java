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
package de.blizzy.documentr;

import java.io.File;
import java.io.IOException;

import javax.annotation.PreDestroy;
import javax.servlet.Filter;

import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.MemoryUnit;

import org.apache.commons.io.FileUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.encoding.PasswordEncoder;
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

import de.blizzy.documentr.access.OpenIdUserDetailsService;
import de.blizzy.documentr.access.Sha512PasswordEncoder;
import de.blizzy.documentr.web.access.DocumentrOpenIdAuthenticationFilter;

@Configuration
@EnableWebMvc
@ComponentScan("de.blizzy.documentr")
@ImportResource({ "classpath:/applicationContext-security.xml", "classpath:/applicationContext-cache.xml" })
public class ContextConfig extends WebMvcConfigurerAdapter {
	private static final String CACHE_DIR_NAME = "cache"; //$NON-NLS-1$
	
	private net.sf.ehcache.CacheManager ehCacheManager;
	
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
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasenames(new String[] { "documentr_messages", "ValidationMessages" }); //$NON-NLS-1$ //$NON-NLS-2$
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
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Sha512PasswordEncoder();
	}
	
	@Bean
	public CacheManager cacheManager(Settings settings) throws IOException {
		File cacheDir = new File(settings.getDocumentrDataDir(), CACHE_DIR_NAME);
		FileUtils.forceMkdir(cacheDir);

		ehCacheManager = net.sf.ehcache.CacheManager.newInstance();
		ehCacheManager.addCache(new Cache(new CacheConfiguration()
			.name("pageHTML") //$NON-NLS-1$
			.diskStorePath(cacheDir.getAbsolutePath())
			.overflowToDisk(true)
			.diskPersistent(true)
			.maxEntriesLocalHeap(1000)
			.maxBytesLocalDisk(100, MemoryUnit.MEGABYTES)
			.timeToIdleSeconds(30L * 24L * 60L * 60L)));
		ehCacheManager.addCache(new Cache(new CacheConfiguration()
			.name("pageViewRestrictionRole") //$NON-NLS-1$
			.diskStorePath(cacheDir.getAbsolutePath())
			.overflowToDisk(true)
			.diskPersistent(true)
			.maxEntriesLocalHeap(1000)
			.maxBytesLocalDisk(10, MemoryUnit.MEGABYTES)
			.timeToIdleSeconds(30L * 24L * 60L * 60L)));
		
		EhCacheCacheManager cacheManager = new EhCacheCacheManager();
		cacheManager.setCacheManager(ehCacheManager);

		return cacheManager;
	}

	@Bean
	public Filter openIdAuthFilter(ProviderManager authManager, RememberMeServices rememberMeServices) {
		DocumentrOpenIdAuthenticationFilter filter = new DocumentrOpenIdAuthenticationFilter();
		filter.setAuthenticationManager(authManager);
		AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/access/login"); //$NON-NLS-1$
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
	
	@PreDestroy
	public void destroy() {
		if (ehCacheManager != null) {
			ehCacheManager.shutdown();
		}
	}
}
