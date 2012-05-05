<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%

int statusCode = ((Integer) request.getAttribute("statusCode")).intValue(); //$NON-NLS-1$
response.sendError(statusCode);

%>