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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li class="active"><c:out value="${name}"/></li>
</dt:breadcrumbs>

<dt:page>

<div class="page-header"><h1><spring:message code="title.branchX" arguments="${name}"/></h1></div>

<h2><spring:message code="title.pages"/></h2>

<c:set var="pagePaths" value="${d:listPagePaths(projectName, name)}"/>
<c:choose>
	<c:when test="${!empty pagePaths}">
		<ul>
		<c:forEach var="path" items="${pagePaths}">
			<li>
				<a href="<c:url value="/page/${projectName}/${name}/${d:toURLPagePath(path)}"/>"><c:out value="${path}"/></a>
				<c:if test="${d:isPageSharedWithOtherBranches(projectName, name, path)}"> <span class="shared-page">(<spring:message code="shared"/>)</span></c:if>
			</li>
		</c:forEach>
		</ul>
	</c:when>
	<c:otherwise><p>No pages found.</p></c:otherwise>
</c:choose>

<sec:authorize access="isAuthenticated()">
	<p>
	<a href="<c:url value="/page/create/${projectName}/${name}"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createPage"/></a>
	</p>
</sec:authorize>

</dt:page>
