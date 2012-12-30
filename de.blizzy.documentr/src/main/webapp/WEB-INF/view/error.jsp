<%--
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
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<%
pageContext.setAttribute("statusCode", request.getAttribute("javax.servlet.error.status_code")); //$NON-NLS-1$ //$NON-NLS-2$
pageContext.setAttribute("message", request.getAttribute("javax.servlet.error.message")); //$NON-NLS-1$ //$NON-NLS-2$
%>

<dt:pageTitle><spring:message code="title.error"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.error"/></h1></div>

<p>
<c:choose>
	<c:when test="${!empty messageKey}"><spring:message code="${messageKey}"/> (<spring:message code="errorX" arguments="${statusCode}"/>)</c:when>
	<c:when test="${!empty message}"><c:out value="${message}"/> (<spring:message code="errorX" arguments="${statusCode}"/>)</c:when>
	<c:otherwise><spring:message code="errorX" arguments="${statusCode}"/></c:otherwise>
</c:choose>
</p>

<p>
<a href="javascript:void(history.go(-1))" class="btn"><spring:message code="button.goBack"/></a>
</p>

</dt:page>
