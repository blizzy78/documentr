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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="(#pageForm.path == null) ?
	hasBranchPermission(#pageForm.projectName, #pageForm.branchName, 'EDIT_PAGE') :
	hasPagePermission(#pageForm.projectName, #pageForm.branchName, #pageForm.path, 'EDIT_PAGE')">

<c:choose>
	<c:when test="${!empty pageForm.path}"><c:set var="hierarchyPagePath" value="${pageForm.path}"/></c:when>
	<c:when test="${!empty pageForm.parentPagePath}"><c:set var="hierarchyPagePath" value="${pageForm.parentPagePath}"/></c:when>
	<c:otherwise><c:set var="hierarchyPagePath" value="home"/></c:otherwise>
</c:choose>

<dt:headerJS>

<c:if test="${empty pageForm.path}">
$(function() {
	var el = $('#pageForm').find('#title');
	el.blur(function() {
		var pinPathButton = $('#pinPathButton');
		if (!pinPathButton.hasClass('active')) {
			var fieldset = $('#pathFieldset');
			fieldset.removeClass('warning').removeClass('error');
			$('#pathExistsWarning').remove();
	
			var value = el.val();
			if (value.length > 0) {
				$.ajax({
					url: '<c:url value="/page/generateName/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(hierarchyPagePath)}/json"/>',
					type: 'POST',
					dataType: 'json',
					data: {
						title: value
					},
					success: function(result) {
						$('#pageForm').find('#path').val(result.path);
						if (result.exists) {
							fieldset.addClass('warning');
							fieldset.append($('<span id="pathExistsWarning" class="help-inline">' +
								'<spring:message code="page.path.exists"/></span>'));
						}
						$('#pinPathButton').removeClass('disabled').removeClass('active');
					}
				});
			}
		}
	});
});
</c:if>

function togglePreview() {
	var previewEl = $('#preview');
	if (previewEl.length == 0) {
		var textEl = $('#pageForm').find('#text');
		$.ajax({
			url: '<c:url value="/page/markdownToHTML/${pageForm.projectName}/${pageForm.branchName}/json"/>',
			type: 'POST',
			dataType: 'json',
			data: {
				pagePath: '<c:out value="${hierarchyPagePath}"/>',
				markdown: textEl.val()
			},
			success: function(result) {
				var previewEl = $('<div id="preview" class="preview"></div>');
				previewEl.html(result.html);
				$(document.body).append(previewEl);
				previewEl.show()
					.css('left', textEl.offset().left)
					.css('top', textEl.offset().top)
					.css('width', textEl.outerWidth() - (previewEl.outerWidth() - previewEl.width()))
					.css('height', textEl.outerHeight() - (previewEl.outerHeight() - previewEl.height()));
				prettyPrint();
				$('#textEditorToolbar a').each(function() {
					$(this).setButtonDisabled(true);
				});
				$('#togglePreviewButton').setButtonDisabled(false);
			}
		});
	} else {
		previewEl.remove();
		$('#textEditorToolbar a').each(function() {
			$(this).setButtonDisabled(false);
		});
	}
}

function toggleStyleBold() {
	var textEl = $('#text');
	var start = textEl[0].selectionStart;
	var end = textEl[0].selectionEnd;
	var text = textEl.val();
	var isBold = (start >= 2) && (end <= (text.length - 2)) &&
		(text.substring(start - 2, start) == '**') &&
		(text.substring(end, end + 2) == '**');
	if (!isBold) {
		text = text.substring(0, start) + '**' + text.substring(start, end) + '**' + text.substring(end);
		start = start + 2;
		end = end + 2;
	} else {
		text = text.substring(0, start - 2) + text.substring(start, end) + text.substring(end + 2);
		start = start - 2;
		end = end - 2;
	}
	textEl.val(text);
	textEl[0].setSelectionRange(start, end);
	textEl.focus();
}

function toggleStyleItalic() {
	var textEl = $('#text');
	var start = textEl[0].selectionStart;
	var end = textEl[0].selectionEnd;
	var text = textEl.val();
	var isItalic = (start >= 1) && (end <= (text.length - 1)) &&
		(text.substring(start - 1, start) == '*') &&
		(text.substring(end, end + 1) == '*');
	if (!isItalic) {
		text = text.substring(0, start) + '*' + text.substring(start, end) + '*' + text.substring(end);
		start++;
		end++;
	} else {
		text = text.substring(0, start - 1) + text.substring(start, end) + text.substring(end + 1);
		start--;
		end--;
	}
	textEl.val(text);
	textEl[0].setSelectionRange(start, end);
	textEl.focus();
}

</dt:headerJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${pageForm.projectName}"/>"><c:out value="${pageForm.projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/home"/>"><c:out value="${pageForm.branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(pageForm.projectName, pageForm.branchName, hierarchyPagePath)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(entry)}"/>"><c:out value="${d:getPageTitle(pageForm.projectName, pageForm.branchName, entry)}"/></a> <span class="divider">/</span></li>
		</c:if>
	</c:forEach>
	<li class="active"><spring:message code="title.editPage"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editPage"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editPage"/></h1></div>

<p>
<c:set var="action"><c:url value="/page/save/${pageForm.projectName}/${pageForm.branchName}"/></c:set>
<form:form commandName="pageForm" action="${action}" method="POST" cssClass="well form-horizontal">
	<fieldset>
		<form:hidden path="parentPagePath"/>
		<c:set var="errorText"><form:errors path="title"/></c:set>
		<div class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="title" cssClass="control-label"><spring:message code="label.title"/>:</form:label>
			<form:input path="title" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<div id="pathFieldset" class="control-group">
			<form:hidden path="path"/>
			<form:label path="path" cssClass="control-label"><spring:message code="label.pathGeneratedAutomatically"/>:</form:label>
			<form:input path="path" cssClass="input-xlarge disabled" disabled="true"/>
			<c:if test="${empty pageForm.path}">
				<a id="pinPathButton" class="btn disabled" data-toggle="button" href="javascript:;" title="<spring:message code="button.pinPath"/>"><i class="icon-lock"></i></a>
			</c:if>
		</div>
		<div class="control-group">
			<form:label path="text" cssClass="control-label"><spring:message code="label.contents"/>:</form:label>
			<div class="texteditor">
				<div id="textEditorToolbar" class="btn-toolbar btn-toolbar-icons">
					<div class="btn-group">
						<a id="togglePreviewButton" href="javascript:togglePreview();" class="btn" data-toggle="button" title="<spring:message code="button.showPreview"/>"><i class="icon-eye-open"></i></a>
					</div>
					<div class="btn-group">
						<a href="javascript:toggleStyleBold();" class="btn" title="<spring:message code="button.bold"/>"><i class="icon-bold"></i></a>
						<a href="javascript:toggleStyleItalic();" class="btn" title="<spring:message code="button.italic"/>"><i class="icon-italic"></i></a>
					</div>
				</div>
				<form:textarea path="text" cssClass="span11 code" rows="20"/>
			</div>
		</div>
		<div class="control-group">
			<form:label path="viewRestrictionRole" cssClass="control-label"><spring:message code="label.visibleForRole"/>:</form:label>
			<form:select path="viewRestrictionRole">
				<form:option value="">(<spring:message code="everyone"/>)</form:option>
				<c:set var="roles" value="${d:listRoles()}"/>
				<form:options items="${roles}"/>
			</form:select>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
			<a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(hierarchyPagePath)}"/>" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</fieldset>
</form:form>
</p>

</dt:page>

</sec:authorize>
