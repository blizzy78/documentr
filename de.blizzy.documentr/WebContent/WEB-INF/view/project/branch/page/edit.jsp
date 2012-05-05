<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<sec:authorize access="isAuthenticated()">

<c:set var="headerJavascript" scope="request">

<c:if test="${empty pageForm.path}">
$(function() {
	var el = $('#pageForm').find('#title');
	el.blur(function() {
		var fieldset = $('#pathFieldset');
		fieldset.removeClass('warning').removeClass('error');
		$('#pathExistsWarning').remove();

		var value = el.val();
		if (value.length > 0) {
			$.ajax({
				url: '<c:url value="/page/generateName/${pageForm.projectName}/${pageForm.branchName}/json"/>',
				type: 'POST',
				dataType: 'json',
				data: {
					title: value
				},
				success: function(result) {
					$('#pageForm').find('#path').val(result.name);
					if (result.exists) {
						fieldset.addClass('warning');
						fieldset.append($('<span id="pathExistsWarning" class="help-inline">' +
							'<spring:message code="page.path.exists"/></span>'));
					}
				}
			});
		}
	});
});
</c:if>

function showPreview() {
	var textEl = $('#pageForm').find('#text');
	$.ajax({
		url: '<c:url value="/page/markdownToHTML/${pageForm.projectName}/${pageForm.branchName}/json"/>',
		type: 'POST',
		dataType: 'json',
		data: {
			<c:if test="${!empty pageForm.path}">
				pagePath: '<c:out value="${pageForm.path}"/>',
			</c:if>
			markdown: textEl.val()
		},
		success: function(result) {
			$('#previewText').html(result.html);
			$('#preview').modal({
				backdrop: true,
				keyboard: true
			});
			$('#preview').position({
				my: 'center center',
				at: 'center center',
				of: window
			});
		}
	});
}

function hidePreview() {
	$('#preview').modal('hide');
}

</c:set>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${pageForm.projectName}"/>"><c:out value="${pageForm.projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/branch/${pageForm.projectName}/${pageForm.branchName}"/>"><c:out value="${pageForm.branchName}"/></a> <span class="divider">/</span></li>
	<c:if test="${!empty pageForm.path}">
		<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(pageForm.path)}"/>"><c:out value="${pageForm.title}"/></a> <span class="divider">/</span></li>
	</c:if>
	<li class="active"><spring:message code="title.editPage"/></li>
</ul>

<div class="page-header"><h1><spring:message code="title.editPage"/></h1></div>

<p>
<c:set var="action"><c:url value="/page/save/${pageForm.projectName}/${pageForm.branchName}"/></c:set>
<form:form commandName="pageForm" action="${action}" method="POST" cssClass="well">
	<c:set var="errorText"><form:errors path="title"/></c:set>
	<fieldset class="control-group <c:if test="${!empty errorText}">error</c:if>">
		<form:label path="title"><spring:message code="label.title"/>:</form:label>
		<form:input path="title" cssClass="input-xlarge"/>
		<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
	</fieldset>
	<fieldset id="pathFieldset" class="control-group">
		<form:label path="path"><spring:message code="label.pathGeneratedAutomatically"/>:</form:label>
		<form:input path="path" cssClass="input-xlarge disabled" disabled="true"/>
		<form:hidden path="path"/>
	</fieldset>
	<fieldset class="control-group">
		<form:label path="text"><spring:message code="label.contents"/>:</form:label>
		<form:textarea path="text" cssClass="span11 code" rows="20"/>
		<a href="javascript:showPreview();" class="btn" title="<spring:message code="button.showPreview"/>"><i class="icon-eye-open"></i></a>
	</fieldset>
	<fieldset class="control-group">
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<c:choose>
			<c:when test="${!empty pageForm.path}">
				<c:set var="pathUrl" value="${d:toURLPagePath(pageForm.path)}"/>
				<a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${pathUrl}"/>" class="btn"><spring:message code="button.cancel"/></a>
			</c:when>
			<c:otherwise>
				<a href="<c:url value="/branch/${pageForm.projectName}/${pageForm.branchName}"/>" class="btn"><spring:message code="button.cancel"/></a>
			</c:otherwise>
		</c:choose>
	</fieldset>
</form:form>
</p>

<div class="modal" id="preview" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="hidePreview();">×</button>
		<h3><spring:message code="title.pagePreview"/></h3>
	</div>
	<div class="modal-body" id="previewText"></div>
	<div class="modal-footer">
		<a href="javascript:hidePreview();" class="btn">Close</a>
	</div>
</div>

<jsp:include page="/WEB-INF/view/footer.jsp"/>

</sec:authorize>
