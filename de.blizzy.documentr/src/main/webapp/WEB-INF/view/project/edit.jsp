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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="isAuthenticated()">

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editProject"/></li>
</dt:breadcrumbs>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editProject"/></h1></div>

<p>
<c:set var="action"><c:url value="/project/save"/></c:set>
<form:form commandName="projectForm" action="${action}" method="POST" cssClass="well">
	<fieldset class="control-group <spring:hasBindErrors name="projectForm">error</spring:hasBindErrors>">
		<form:label path="name"><spring:message code="label.name"/>:</form:label>
		<form:input path="name" cssClass="input-xlarge"/>
		<spring:hasBindErrors name="projectForm"><span class="help-inline"><form:errors path="name"/></span></spring:hasBindErrors>
	</fieldset>
	<fieldset class="control-group">
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<a href="<c:url value="/"/>" class="btn"><spring:message code="button.cancel"/></a>
	</fieldset>
</form:form>
</p>

</dt:page>

</sec:authorize>
