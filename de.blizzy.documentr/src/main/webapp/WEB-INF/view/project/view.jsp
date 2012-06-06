<%--
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
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasProjectPermission(#name, 'VIEW')">

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li class="active"><c:out value="${name}"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.projectX" arguments="${name}"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.projectX" arguments="${name}"/></h1></div>

<h2><spring:message code="title.branches"/></h2>

<c:set var="branches" value="${d:listProjectBranches(name)}"/>
<c:choose>
	<c:when test="${!empty branches}">
		<ul>
		<c:forEach var="branch" items="${branches}">
			<li><a href="<c:url value="/page/${name}/${branch}/home"/>"><c:out value="${branch}"/></a></li>
		</c:forEach>
		</ul>
	</c:when>
	<c:otherwise><p>No branches found.</p></c:otherwise>
</c:choose>

<sec:authorize access="hasProjectPermission(#name, 'EDIT_BRANCH')">
	<p>
	<a href="<c:url value="/branch/create/${name}"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createBranch"/></a>
	</p>
</sec:authorize>

</dt:page>

</sec:authorize>
