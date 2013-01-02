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

<li class="btn-toolbar pull-right">
	<div class="btn-group">
		<form id="siteSearch" class="form-search" action="<c:url value="/search/page"/>" method="GET">
			<input type="text" name="q" class="search-query input-large" value="<c:out value="${requestScope._showSiteSearch}"/>"
				placeholder="<spring:message code="searchTermsPlaceholder"/>" x-webkit-speech="x-webkit-speech"/>
		</form>
	</div>

	<sec:authorize access="isAuthenticated()">
		<div class="btn-group">
			<a class="btn btn-mini dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-cog"></i> <spring:message code="button.administration"/> <span class="caret"></span></a>
			<ul class="dropdown-menu">
				<li><a href="<c:url value="/projects"/>"><i class="icon-folder-open"></i> <spring:message code="button.projects"/></a></li>
	
				<sec:authorize access="hasApplicationPermission(ADMIN)">
					<li><a href="<c:url value="/users"/>"><i class="icon-user"></i> <spring:message code="button.users"/></a></li>
					<li><a href="<c:url value="/system/edit"/>"><i class="icon-cog"></i> <spring:message code="button.systemSettings"/></a></li>
					<li><a href="<c:url value="/macros"/>"><i class="icon-play-circle"></i> <spring:message code="button.macros"/></a></li>
				</sec:authorize>
			</ul>
		</div>
	</sec:authorize>

	<sec:authorize access="isAuthenticated()">
		<div class="btn-group">
			<c:set var="loginName"><sec:authentication property="principal.username"/></c:set>
			<a href="<c:url value="/j_spring_security_logout"/>" class="btn btn-mini"><i class="icon-off"></i> <spring:message code="button.logoutUserX" arguments="${loginName}"/></a>
			<button class="btn btn-mini dropdown-toggle" data-toggle="dropdown"><span class="caret"></span></button>
			<ul class="dropdown-menu">
				<li><a href="<c:url value="/account/myAccount"/>"><i class="icon-user"></i> <spring:message code="button.myAccount"/></a></li>
			</ul>
		</div>
	</sec:authorize>

	<sec:authorize access="isAnonymous()">
		<div class="btn-group">
			<a href="<c:url value="/access/login"/>" class="btn btn-mini"><i class="icon-lock"></i> <spring:message code="button.login"/></a>
		</div>
	</sec:authorize>
</li>
