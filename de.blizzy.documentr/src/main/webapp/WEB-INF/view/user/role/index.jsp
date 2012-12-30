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
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasApplicationPermission(ADMIN)">

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.roles"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.roles"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.accountManagement"/></h1></div>

<ul class="nav nav-tabs">
	<li><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a></li>
	<li class="active"><a href="<c:url value="/roles"/>"><spring:message code="title.roles"/></a></li>
</ul>

<h2><spring:message code="title.roles"/></h2>

<c:set var="roles" value="${d:listRoles()}"/>
<ul>
<c:forEach var="role" items="${roles}">
	<li><a href="<c:url value="/role/edit/${role}"/>"><c:out value="${role}"/></a></li>
</c:forEach>
</ul>

<p>
<a href="<c:url value="/role/add"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.addRole"/></a>
</p>

</dt:page>

</sec:authorize>
