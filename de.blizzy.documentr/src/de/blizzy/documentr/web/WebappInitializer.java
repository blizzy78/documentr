package de.blizzy.documentr.web;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import de.blizzy.documentr.ContextConfig;
import de.blizzy.documentr.web.filter.RequestEncodingFilter;

public class WebappInitializer implements WebApplicationInitializer {
	@Override
	public void onStartup(ServletContext context) throws ServletException {
		AnnotationConfigWebApplicationContext appContext = new AnnotationConfigWebApplicationContext();
		appContext.setServletContext(context);
		appContext.setConfigLocation(ContextConfig.class.getName());
		
		DispatcherServlet dispatcherServlet = new DispatcherServlet(appContext);
		ServletRegistration.Dynamic dispatcher = context.addServlet("dispatcher", dispatcherServlet); //$NON-NLS-1$
		dispatcher.setLoadOnStartup(1);
		dispatcher.addMapping("/"); //$NON-NLS-1$

		FilterRegistration.Dynamic encodingFilterConfig =
				context.addFilter("requestEncodingFilter", RequestEncodingFilter.class); //$NON-NLS-1$
		encodingFilterConfig.addMappingForUrlPatterns(null, true, "/*"); //$NON-NLS-1$
		
		DelegatingFilterProxy securityFilter =
				new DelegatingFilterProxy("springSecurityFilterChain", appContext); //$NON-NLS-1$
		FilterRegistration.Dynamic securityFilterConfig =
				context.addFilter("springSecurityFilterChain", securityFilter); //$NON-NLS-1$
		securityFilterConfig.addMappingForUrlPatterns(null, true, "/*");  //$NON-NLS-1$
	}
}
