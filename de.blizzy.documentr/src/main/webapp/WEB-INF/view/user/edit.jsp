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
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasApplicationPermission('ADMIN')">

<dt:headerJSFile uri="/js/zxcvbn-20120527.js"/>

<dt:headerJS>

function updatePasswordStrengthIndicator() {
	var result = zxcvbn($('#password1').val(), [ 'documentr' ]);

	$('#password1Error').remove();

	var indicator = $('#passwordStrengthIndicator');
	if (indicator.length == 0) {
		indicator = $('<span class="help-inline" id="passwordStrengthIndicator"><div class="progress password-strength-indicator"><div class="bar"></div></div></span>');
		$('#password1Fieldset').append(indicator);
	}

	indicator.find('.bar').width(((result.score + 1) * 20) + '%');
	indicator.removeClass('progress-success').removeClass('progress-warning').removeClass('progress-danger');
	if (result.score <= 1) {
		indicator.addClass('progress-danger');
	} else if (result.score <= 3) {
		indicator.addClass('progress-warning');
	} else {
		indicator.addClass('progress-success');
	}
}

</dt:headerJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editUser"/></li>
</dt:breadcrumbs>

<dt:page>

<div class="page-header"><h1><spring:message code="title.accountManagement"/></h1></div>

<ul class="nav nav-tabs">
	<li class="active"><a href="<c:url value="/users"/>"><spring:message code="title.users"/></a></li>
	<li><a href="<c:url value="/roles"/>"><spring:message code="title.roles"/></a></li>
</ul>

<h2><spring:message code="title.editUser"/></h2>

<p>
<c:set var="action"><c:url value="/user/save"/></c:set>
<form:form commandName="userForm" action="${action}" method="POST" cssClass="well form-horizontal">
	<fieldset>
		<c:set var="errorText"><form:errors path="loginName"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="loginName" cssClass="control-label"><spring:message code="label.loginName"/>:</form:label>
			<c:choose>
				<c:when test="${(!empty userForm.loginName) && (empty errorText)}">
					<form:hidden path="loginName"/>
					<form:input path="loginName" cssClass="input-xlarge disabled" disabled="true"/>
				</c:when>
				<c:otherwise>
					<form:input path="loginName" cssClass="input-xlarge"/>
				</c:otherwise>
			</c:choose>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<c:set var="errorText1"><form:errors path="password1"/></c:set>
		<c:set var="errorText2"><form:errors path="password2"/></c:set>
		<div id="password1Fieldset" class="control-group <c:if test="${!empty errorText1 or !empty errorText2}">error</c:if>">
			<form:label path="password1" cssClass="control-label"><spring:message code="label.password"/>:</form:label>
			<form:password path="password1" cssClass="input-xlarge" autocomplete="off" onkeyup="updatePasswordStrengthIndicator()"/>
			<c:if test="${!empty errorText1}"><span class="help-inline" id="password1Error"><c:out value="${errorText1}" escapeXml="false"/></span></c:if>
		</div>
		<div class="control-group <c:if test="${!empty errorText1 or !empty errorText2}">error</c:if>">
			<form:label path="password2" cssClass="control-label"><spring:message code="label.repeatPassword"/>:</form:label>
			<form:password path="password2" cssClass="input-xlarge" autocomplete="off"/>
			<c:if test="${!empty errorText2}"><span class="help-inline"><c:out value="${errorText2}" escapeXml="false"/></span></c:if>
		</div>
		<c:set var="errorText"><form:errors path="email"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="email" cssClass="control-label"><spring:message code="label.email"/>:</form:label>
			<form:input path="email" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<div class="control-group">
			<form:label path="disabled" cssClass="checkbox">
				<form:checkbox path="disabled"/>
				<spring:message code="label.accountDisabled"/>
			</form:label>
		</div>
		<div class="control-group">
			<form:label path="admin" cssClass="checkbox">
				<form:checkbox path="admin"/>
				<spring:message code="label.adminPermissions"/>
			</form:label>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
			<a href="<c:url value="/users"/>" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</fieldset>
</form:form>
</p>

</dt:page>

</sec:authorize>
