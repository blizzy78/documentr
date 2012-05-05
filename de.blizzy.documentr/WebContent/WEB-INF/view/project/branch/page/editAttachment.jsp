<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>
<sec:authorize access="isAuthenticated()">

<c:set var="pagePathUrl" value="${d:toURLPagePath(pagePath)}"/>
<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/branch/${projectName}/${branchName}"/>"><c:out value="${branchName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${projectName}/${branchName}/${pagePathUrl}"/>"><c:out value="${d:getPageTitle(projectName, branchName, pagePath)}"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editAttachment"/></li>
</dt:breadcrumbs>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editAttachment"/></h1></div>

<form action="<c:url value="/attachment/save/${projectName}/${branchName}/${pagePathUrl}"/>"
	method="post" enctype="multipart/form-data" class="well">
	
	<input type="hidden" name="projectName" value="<c:out value="${projectName}"/>"/>
	<input type="hidden" name="branchName" value="<c:out value="${branchName}"/>"/>
	<input type="hidden" name="pagePath" value="<c:out value="${pagePath}"/>"/>

	<fieldset class="control-group">
		<label>File:</label>
		<input type="file" name="file" class="input-file"/>
	</fieldset>
	<fieldset class="control-group">
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<a href="<c:url value="/page/${projectName}/${branchName}/${pagePathUrl}"/>" class="btn"><spring:message code="button.cancel"/></a>
	</fieldset>
</form>

</dt:page>

</sec:authorize>
