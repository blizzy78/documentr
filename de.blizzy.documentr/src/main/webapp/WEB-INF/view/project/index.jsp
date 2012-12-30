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

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.projects"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.projects"/></dt:pageTitle>

<dt:page>

<c:set var="projects" value="${d:listProjects()}"/>
<c:choose>
	<c:when test="${!empty projects}">
		
		<div class="page-header"><h1><spring:message code="title.projects"/></h1></div>

		<ul>
		<c:forEach var="project" items="${projects}">
			<sec:authorize access="hasProjectPermission(#project, VIEW)">
				<li><a href="<c:url value="/project/${project}"/>"><c:out value="${project}"/></a></li>
			</sec:authorize>
		</c:forEach>
		</ul>

		<sec:authorize access="hasApplicationPermission(EDIT_PROJECT)">
			<p>
			<a href="<c:url value="/project/create"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createProject"/></a>
			</p>
		</sec:authorize>
	</c:when>
	<c:otherwise>
		<div class="row">
			<div class="hero-unit span8 offset2 hero-single">
				<h1>documentr</h1>
				<p><spring:message code="welcomeToDocumentr" htmlEscape="false"/></p>
				<p>
					<sec:authorize access="isAnonymous()">
						<a href="<c:url value="/access/login"/>" class="btn btn-primary btn-large"><spring:message code="button.login"/></a>
					</sec:authorize>
					<sec:authorize access="hasApplicationPermission(EDIT_PROJECT)">
						<a href="<c:url value="/project/create"/>" class="btn btn-primary btn-large"><spring:message code="button.createFirstProject"/></a>
					</sec:authorize>
				</p>
			</div>
		</div>
	</c:otherwise>
</c:choose>

</dt:page>
