package de.blizzy.documentr.web;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {
	@RequestMapping("/{statusCode:[0-9]+}/{messageKey:[a-zA-Z0-9\\.]+}")
	public String sendError(@PathVariable int statusCode, @PathVariable String messageKey, Model model) {
		model.addAttribute("statusCode", Integer.valueOf(statusCode)); //$NON-NLS-1$
		model.addAttribute("messageKey", messageKey); //$NON-NLS-1$
		return "/sendError"; //$NON-NLS-1$
	}
	
	public static String notFound(String messageKey) {
		Assert.hasLength(messageKey);
		
		return "forward:/error/" + HttpServletResponse.SC_NOT_FOUND + "/" + messageKey; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
