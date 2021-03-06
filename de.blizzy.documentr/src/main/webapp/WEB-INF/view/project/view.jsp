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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="hasProjectPermission(#name, VIEW)">

<c:set var="branches" value="${d:listProjectBranches(name)}"/>

<dt:pageJS>

<c:if test="${empty branches}">
<sec:authorize access="hasProjectPermission(#name, ADMIN)">

function importSampleContents() {
	require(['documentr/dialog'], function(dialog) {
		new dialog.Dialog()
			.title('<spring:message code="title.importSampleContents"/>')
			.message("<spring:message code="importSampleContents" arguments="${name}"/>")
			.button(new dialog.DialogButton()
				.text('<spring:message code="button.import"/>')
				.click(function(button, dlg) {
					dlg.message('<spring:message code="importingContents"/>');
					dlg.allButtonsDisabled(true);

					$.ajax({
						url: '<c:url value="/project/importSample/${name}/json"/>',
						type: 'GET',
						dataType: 'json',
						success: function() {
							window.location.reload(true);
						}
					});
				})
				.primary())
			.button(new dialog.DialogButton().cancel())
			.show();
	});
}

</sec:authorize>
</c:if>

<sec:authorize access="hasApplicationPermission(EDIT_PROJECT)">
function deleteProject() {
	require(['documentr/dialog'], function(dialog) {
		new dialog.Dialog()
			.title('<spring:message code="title.deleteProject"/>')
			<c:set var="text"><spring:message code="deleteProjectX.html" arguments="${name}"/></c:set>
			.htmlMessage('<c:out value="${fn:replace(text, &quot;'&quot;, &quot;\\\\'&quot;)}" escapeXml="false"/>')
			.button(new dialog.DialogButton()
				.text('<spring:message code="button.delete"/>')
				.click(function(button, dlg) {
					dlg.allButtonsDisabled(true);
					window.location.href = '<c:url value="/project/delete/${name}"/>';
				})
				.danger())
			.button(new dialog.DialogButton().cancel())
			.show();
	});
}
</sec:authorize>

<sec:authorize access="hasProjectPermission(#name, EDIT_BRANCH)">
function deleteBranch(branch) {
	require(['documentr/dialog'], function(dialog) {
		new dialog.Dialog()
			.title('<spring:message code="title.deleteBranch"/>')
			<c:set var="text"><spring:message code="deleteBranchX.html" arguments="__BRANCHNAME__"/></c:set>
			.htmlMessage('<c:out value="${fn:replace(text, &quot;'&quot;, &quot;\\\\'&quot;)}" escapeXml="false"/>'.replace(/__BRANCHNAME__/, branch))
			.button(new dialog.DialogButton()
				.text('<spring:message code="button.delete"/>')
				.click(function(button, dlg) {
					dlg.allButtonsDisabled(true);
					window.location.href = '<c:url value="/branch/delete/${name}/"/>' + branch;
				})
				.danger())
			.button(new dialog.DialogButton().cancel())
			.show();
	});
}
</sec:authorize>

</dt:pageJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li class="active"><c:out value="${name}"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.projectX" arguments="${name}"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.projectX" arguments="${name}"/></h1></div>

<c:set var="buttons">
	<sec:authorize access="hasProjectPermission(#name, EDIT_PROJECT)">
		<a href="<c:url value="/project/edit/${name}"/>" class="btn"><spring:message code="button.editProject"/></a>
	</sec:authorize>
	<sec:authorize access="hasApplicationPermission(EDIT_PROJECT)">
		<a href="javascript:void(deleteProject());" class="btn btn-warning"><spring:message code="button.deleteProject"/></a>
	</sec:authorize>
	<c:if test="${empty branches}">
		<sec:authorize access="hasProjectPermission(#name, ADMIN)">
			<a href="javascript:void(importSampleContents());" class="btn"><i class="icon-download-alt"></i> <spring:message code="button.importSampleContents"/></a>
		</sec:authorize>
	</c:if>
</c:set>
<c:if test="${!empty buttons}">
	<p>
	<c:out value="${buttons}" escapeXml="false"/>
	</p>
</c:if>

<h2><spring:message code="title.branches"/></h2>

<c:choose>
	<c:when test="${!empty branches}">
		<ul>
		<c:forEach var="branch" items="${branches}">
			<c:set var="buttons">
				<sec:authorize access="hasBranchPermission(#name, #branch, EDIT_BRANCH)">
					<a href="<c:url value="/branch/edit/${name}/${branch}"/>"><spring:message code="button.edit"/></a>,
				</sec:authorize>
				<sec:authorize access="hasProjectPermission(#name, EDIT_BRANCH)">
					<a href="javascript:void(deleteBranch('${branch}'));"><spring:message code="button.delete"/></a>,
				</sec:authorize>
			</c:set>
			<li>
				<a href="<c:url value="/page/${name}/${branch}/home"/>"><c:out value="${branch}"/></a>
				<c:if test="${!empty buttons}">
					(<c:out value="${fn:trim(d:substringBeforeLast(fn:trim(buttons), ','))}" escapeXml="false"/>)
				</c:if>
			</li>
		</c:forEach>
		</ul>
	</c:when>
	<c:otherwise><p>No branches found.</p></c:otherwise>
</c:choose>

<sec:authorize access="hasProjectPermission(#name, EDIT_BRANCH)">
	<p>
	<a href="<c:url value="/branch/create/${name}"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createBranch"/></a>
	</p>
</sec:authorize>

</dt:page>

</sec:authorize>
