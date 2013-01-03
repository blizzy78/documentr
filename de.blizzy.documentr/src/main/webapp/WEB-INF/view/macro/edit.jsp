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
var verifyTimeout = null;

function verify() {
	verifyTimeout = null;

	var editor = $('#editor').data('editor');
	var code = editor.getValue();
	$.ajax({
		url: '<c:url value="/macro/verify/json"/>',
		type: 'POST',
		dataType: 'json',
		data: {
			code: code
		},
		success: function(result) {
			showErrors(result.messages);
		}
	});
}

function showErrors(messages) {
	var editor = $('#editor').data('editor');
	var annotations = [];
	if (documentr.isSomething(messages)) {
		for (var i = 0; i < messages.length; i++) {
			annotations.push(toAceAnnotation(messages[i]));
		}
	}
	editor.session.setAnnotations(annotations);
}

function toAceAnnotation(message) {
	return {
		type: message.type.toLowerCase(),
		row: message.startLine - 1,
		column: message.startColumn - 1,
		text: message.message
	};
}

function startVerifyTimeout() {
	if (documentr.isSomething(verifyTimeout)) {
		window.clearTimeout(verifyTimeout);
	}
	
	verifyTimeout = window.setTimeout(verify, 800);
}

function prepareForm() {
	var code = $('#editor').data('editor').getValue();
	$('#macroForm input[name="code"]').val(code);
	dirty = false;
	return true;
}

$(function() {
	require(['ace'], function(ace) {
		var editor = ace.edit('editor');
		$('#editor').data('editor', editor);
		editor.setTheme('ace/theme/chrome');
		editor.session.setMode('ace/mode/groovy');
		editor.setDisplayIndentGuides(true);
		editor.renderer.setShowGutter(true);
		editor.session.setUseWrapMode(false);
		editor.session.setWrapLimitRange(null, null);
		editor.renderer.setShowPrintMargin(false);
		editor.session.setUseSoftTabs(false);
		editor.setHighlightSelectedWord(false);
		
		editor.session.on('change', function() {
			startVerifyTimeout();
			dirty = true;
		});

		verify();
	});

	$(window).bind('beforeunload', function() {
		if (dirty) {
			return '<spring:message code="confirmLeavePage"/>';
		}
	});
	
	$('#macroForm input').on('keypress', function() {
		dirty = true;
	});
});

</dt:pageJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/macros"/>"><spring:message code="title.macros"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editMacro"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editMacro"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editMacro"/></h1></div>

<c:set var="action"><c:url value="/macro/save"/></c:set>
<form:form commandName="macroForm" action="${action}" method="POST" cssClass="well form-horizontal" onsubmit="prepareForm();">
	<fieldset>
		<input type="hidden" name="code"/>
	
		<c:set var="errorText"><form:errors path="name"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="name" cssClass="control-label"><spring:message code="label.name"/>:</form:label>
			<c:choose>
				<c:when test="${(!empty macroForm.name) && (empty errorText)}">
					<form:hidden path="name"/>
					<form:input path="name" cssClass="input-xlarge disabled" disabled="true"/>
				</c:when>
				<c:otherwise>
					<form:input path="name" cssClass="input-xlarge"/>
				</c:otherwise>
			</c:choose>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<div class="control-group">
			<form:label path="code" cssClass="control-label"><spring:message code="label.groovyCode"/>:</form:label>
			<div class="editor-wrapper">
				<!--__NOTRIM__--><div id="editor"><c:out value="${macroForm.code}"/></div><!--__/NOTRIM__-->
			</div>
		</div>
	</fieldset>

	<div class="form-actions">
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<a href="<c:url value="/macros"/>" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</form:form>

</dt:page>

</sec:authorize>
