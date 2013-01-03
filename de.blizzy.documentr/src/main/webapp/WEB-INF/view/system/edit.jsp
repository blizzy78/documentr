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

<sec:authorize access="hasApplicationPermission(ADMIN)">

<dt:pageJS>

var dirty = false;

function clearDirty() {
	dirty = false;
}

$(function() {
	$(window).bind('beforeunload', function() {
		if (dirty) {
			return '<spring:message code="confirmLeavePage"/>';
		}
	});
	
	$('#systemSettingsForm input, #systemSettingsForm select, #systemSettingsForm textarea')
		.on('keypress change select', function() {
			dirty = true;
		});
});

</dt:pageJS>

<dt:breadcrumbs>
	<li class="active"><spring:message code="title.systemSettings"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.systemSettings"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.systemSettings"/></h1></div>

<c:set var="action"><c:url value="/system/save"/></c:set>
<form:form commandName="systemSettingsForm" action="${action}" method="POST" cssClass="well form-horizontal" onsubmit="clearDirty(); return true;">
	<fieldset>
		<legend><spring:message code="title.general"/></legend>
	
		<c:set var="errorText"><form:errors path="documentrHost"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="documentrHost" cssClass="control-label"><spring:message code="label.documentrHost"/>:</form:label>
			<form:input path="documentrHost" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
			<span class="help-block"><spring:message code="documentrHostHelp"/></span>
		</div>
		<c:set var="errorText"><form:errors path="siteNotice"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="siteNotice" cssClass="control-label"><spring:message code="label.siteNotice"/>:</form:label>
			<form:input path="siteNotice" cssClass="input-xxlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
			<span class="help-block"><spring:message code="siteNoticeHelp"/></span>
		</div>
		<c:set var="errorText"><form:errors path="pageFooterHtml"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="pageFooterHtml" cssClass="control-label"><spring:message code="label.pageFooterHtml"/>:</form:label>
			<form:textarea path="pageFooterHtml" cssClass="input-xxlarge" rows="6"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
			<span class="help-block"><spring:message code="pageFooterHtmlHelp"/></span>
		</div>
		<div class="control-group">
			<form:label path="updateCheckInterval" cssClass="control-label"><spring:message code="label.checkForUpdates"/>:</form:label>
			<form:select path="updateCheckInterval">
				<form:option value="never"><spring:message code="never"/></form:option>
				<form:option value="daily"><spring:message code="daily"/></form:option>
				<form:option value="weekly"><spring:message code="weekly"/></form:option>
			</form:select>
			<span class="help-block"><spring:message code="checkForUpdatesHelp"/></span>
		</div>
	</fieldset>
	
	<fieldset>
		<legend><spring:message code="title.sendingMail"/></legend>
	
		<c:set var="errorText"><form:errors path="mailHostName"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="mailHostName" cssClass="control-label"><spring:message code="label.hostName"/>:</form:label>
			<form:input path="mailHostName" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<c:set var="errorText"><form:errors path="mailHostPort"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="mailHostPort" cssClass="control-label"><spring:message code="label.portNumber"/>:</form:label>
			<form:input path="mailHostPort" cssClass="input-mini"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<c:set var="errorText"><form:errors path="mailSenderEmail"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="mailSenderEmail" cssClass="control-label"><spring:message code="label.senderEmail"/>:</form:label>
			<form:input path="mailSenderEmail" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<c:set var="errorText"><form:errors path="mailSenderName"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="mailSenderName" cssClass="control-label"><spring:message code="label.senderName"/>:</form:label>
			<form:input path="mailSenderName" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<c:set var="errorText"><form:errors path="mailSubjectPrefix"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="mailSubjectPrefix" cssClass="control-label"><spring:message code="label.subjectPrefix"/>:</form:label>
			<form:input path="mailSubjectPrefix" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<div class="control-group">
			<form:label path="mailDefaultLanguage" cssClass="control-label"><spring:message code="label.defaultLanguage"/>:</form:label>
			<form:select path="mailDefaultLanguage">
				<form:option value="de">de - <spring:message code="language.de"/></form:option>
				<form:option value="en">en - <spring:message code="language.en"/></form:option>
			</form:select>
		</div>
	</fieldset>

	<fieldset>
		<legend><spring:message code="title.security"/></legend>
	
		<c:set var="errorText"><form:errors path="bcryptRounds"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="bcryptRounds" cssClass="control-label"><spring:message code="label.bcryptRounds"/>:</form:label>
			<form:input path="bcryptRounds" cssClass="input-mini"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
			<span class="help-block"><spring:message code="bcryptRoundsHelp"/></span>
		</div>
	</fieldset>

	<c:forEach var="macroSettings" items="${systemSettingsForm.macroSettings}">
		<c:set var="macroName" value="${macroSettings.key}"/>
		<fieldset>
			<c:set var="macroTitle"><spring:message code="macro.${macroName}.title" text="${macroName}"/></c:set>
			<legend><spring:message code="title.macroX" arguments="${macroTitle}" argumentSeparator="__DUMMY__"/></legend>
			
			<c:forEach var="setting" items="${macroSettings.value}">
				<div class="control-group">
					<label for="<c:out value="macro.${macroName}.${setting.key}"/>" class="control-label"><spring:message code="macro.${macroName}.setting.${setting.key}" text="${setting.key}"/>:</label>
					<input name="<c:out value="macro.${macroName}.${setting.key}"/>" id="<c:out value="macro.${macroName}.${setting.key}"/>" type="text" class="input-xlarge" value="<c:out value="${setting.value}"/>"/>
				</div>
			</c:forEach>
		</fieldset>
	</c:forEach>

	<div class="form-actions">
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
	</div>
</form:form>

</dt:page>

</sec:authorize>
