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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="isAuthenticated()">

<dt:pageJS>

function updatePasswordStrengthIndicator() {
	require(['zxcvbn'], function(zxcvbn) {
		var result = zxcvbn($('#newPassword1').val(), [ 'documentr' ]);
	
		$('#newPassword1Error').remove();
	
		var indicator = $('#passwordStrengthIndicator');
		if (indicator.length === 0) {
			indicator = $('<span class="help-inline" id="passwordStrengthIndicator"><div class="progress password-strength-indicator"><div class="bar"></div></div></span>');
			$('#newPassword1Fieldset').append(indicator);
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
	});
}

</dt:pageJS>

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

<c:if test="${!empty messageKey}">
	<div class="alert alert-success"><spring:message code="${messageKey}"/></div>
</c:if>

<c:set var="action"><c:url value="/account/save"/></c:set>
<form:form commandName="accountForm" action="${action}" method="POST" cssClass="well form-horizontal">
	<fieldset>
		<legend><spring:message code="title.changePassword"/></legend>
	
		<c:set var="errorText"><form:errors path="password"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="password" cssClass="control-label"><spring:message code="label.oldPassword"/>:</form:label>
			<form:password path="password" cssClass="input-xlarge" autocomplete="off"/>
			<spring:hasBindErrors name="accountForm"><span class="help-inline"><form:errors path="password"/></span></spring:hasBindErrors>
		</div>
		<c:set var="errorText1"><form:errors path="newPassword1"/></c:set>
		<c:set var="errorText2"><form:errors path="newPassword2"/></c:set>
		<div id="newPassword1Fieldset" class="control-group <c:if test="${!empty errorText1 or !empty errorText2}">error</c:if>">
			<form:label path="newPassword1" cssClass="control-label"><spring:message code="label.newPassword"/>:</form:label>
			<form:password path="newPassword1" cssClass="input-xlarge" autocomplete="off" onkeyup="updatePasswordStrengthIndicator()"/>
			<spring:hasBindErrors name="accountForm"><span id="newPassword1Error" class="help-inline"><form:errors path="newPassword1"/></span></spring:hasBindErrors>
		</div>
		<div class="control-group <c:if test="${!empty errorText1 or !empty errorText2}">error</c:if>">
			<form:label path="newPassword2" cssClass="control-label"><spring:message code="label.repeatPassword"/>:</form:label>
			<form:password path="newPassword2" cssClass="input-xlarge" autocomplete="off"/>
			<spring:hasBindErrors name="accountForm"><span class="help-inline"><form:errors path="newPassword2"/></span></spring:hasBindErrors>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		</div>
	</fieldset>
</form:form>

</dt:page>

</sec:authorize>
