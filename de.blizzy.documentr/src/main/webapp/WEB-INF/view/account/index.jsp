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

<sec:authorize access="isAuthenticated()">

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.userAccount"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.userAccount"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.userAccount"/></h1></div>

<ul class="nav nav-tabs">
	<li class="active"><a href="<c:url value="/account/myAccount"/>"><spring:message code="title.accountData"/></a></li>
	<li><a href="<c:url value="/account/openId"/>"><spring:message code="title.openId"/></a></li>
</ul>

<h2><spring:message code="title.accountData"/></h2>

</dt:page>

</sec:authorize>
