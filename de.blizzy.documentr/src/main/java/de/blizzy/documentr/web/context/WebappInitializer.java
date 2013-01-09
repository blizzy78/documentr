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
package de.blizzy.documentr.web.context;

import java.util.EnumSet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionTrackingMode;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import de.blizzy.documentr.context.ContextConfig;
import de.blizzy.documentr.context.EventBusBeanFactoryPostProcessor;
import de.blizzy.documentr.web.filter.RequestEncodingFilter;
import de.blizzy.documentr.web.filter.TrimFilter;

@Slf4j
public class WebappInitializer implements WebApplicationInitializer {
	@Override
	public void onStartup(ServletContext context) {
		log.info("initializing documentr web application"); //$NON-NLS-1$
		WebApplicationContext appContext = setupApplicationContext(context);
		setupServletContext(context, appContext);
		log.info("web application initialization complete"); //$NON-NLS-1$
	}

	private WebApplicationContext setupApplicationContext(ServletContext context) {
		log.debug("initializing Spring application context"); //$NON-NLS-1$

		AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
		appContext.setServletContext(context);
		appContext.setConfigLocation(ContextConfig.class.getName());
		appContext.addBeanFactoryPostProcessor(new EventBusBeanFactoryPostProcessor());
		appContext.refresh();
		appContext.start();
		return appContext;
	}

	private void setupServletContext(ServletContext context, WebApplicationContext appContext) {
		log.debug("initializing servlet context"); //$NON-NLS-1$

		context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, appContext);

		context.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));

		DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
		ServletRegistration.Dynamic dispatcher = context.addServlet("dispatcher", dispatcherServlet); //$NON-NLS-1$
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/"); //$NON-NLS-1$

		FilterRegistration.Dynamic encodingFilterConfig =
				context.addFilter("requestEncodingFilter", RequestEncodingFilter.class); //$NON-NLS-1$
		encodingFilterConfig.addMappingForUrlPatterns(null, true, "/*"); //$NON-NLS-1$

		FilterRegistration.Dynamic trimFilterConfig = context.addFilter("trimFilter", TrimFilter.class); //$NON-NLS-1$
		trimFilterConfig.addMappingForUrlPatterns(null, true, "/*"); //$NON-NLS-1$

		DelegatingFilterProxy securityFilter =
				new DelegatingFilterProxy("springSecurityFilterChain", appContext); //$NON-NLS-1$
		FilterRegistration.Dynamic securityFilterConfig =
				context.addFilter("springSecurityFilterChain", securityFilter); //$NON-NLS-1$
		securityFilterConfig.addMappingForUrlPatterns(null, true, "/*");  //$NON-NLS-1$
	}
}
