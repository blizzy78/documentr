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

<dt:pageJS>

function deleteMacro(name) {
	require(['documentr/dialog'], function(dialog) {
		dialog.openMessageDialog('<spring:message code="title.deleteMacro"/>',
			"<spring:message code="deleteMacroX" arguments="_NAME_"/>".replace(/_NAME_/, name), [
			{
				text: '<spring:message code="button.delete"/>',
				type: 'danger',
				href: '<c:url value="/macro/delete/"/>' + name
			},
			{
				text: '<spring:message code="button.cancel"/>',
				cancel: true
			}
		]);
	});
}

</dt:pageJS>

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.macros"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.macros"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.macros"/></h1></div>

<c:set var="macros" value="${d:getGroovyMacros()}"/>
<ul>
<c:forEach var="macro" items="${macros}">
	<li><a href="<c:url value="/macro/edit/${macro}"/>"><c:out value="${macro}"/></a> (<a href="javascript:void(deleteMacro('${macro}'));"><spring:message code="button.delete"/></a>)</li>
</c:forEach>
</ul>

<p>
<a href="<c:url value="/macro/create"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createMacro"/></a>
</p>

</dt:page>

</sec:authorize>
