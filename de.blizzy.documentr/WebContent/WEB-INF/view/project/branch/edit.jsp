<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${branchForm.projectName}"/>"><spring:message code="title.projectX" arguments="${branchForm.projectName}"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.editBranch"/></li>
</ul>

<div class="page-header"><h1><spring:message code="title.editBranch"/></h1></div>

<p>
<c:set var="action"><c:url value="/branch/save/${branchForm.projectName}"/></c:set>
<form:form commandName="branchForm" action="${action}" method="POST" cssClass="well">
	<fieldset class="control-group <spring:hasBindErrors name="branchForm">error</spring:hasBindErrors>">
		<form:label path="name"><spring:message code="label.name"/>:</form:label>
		<form:input path="name" cssClass="input-xlarge"/>
		<spring:hasBindErrors name="branchForm"><span class="help-inline"><form:errors path="name"/></span></spring:hasBindErrors>
	</fieldset>
	<c:set var="branches" value="${d:listProjectBranches(branchForm.projectName)}"/>
	<c:if test="${!empty branches}">
		<fieldset>
			<form:label path="startingBranch"><spring:message code="label.branchFrom"/>:</form:label>
			<form:select path="startingBranch">
				<form:options items="${branches}"/>
			</form:select>
		</fieldset>
	</c:if>
	<fieldset>
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<a href="<c:url value="/project/${branchForm.projectName}"/>" class="btn"><spring:message code="button.cancel"/></a>
	</fieldset>
</form:form>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
