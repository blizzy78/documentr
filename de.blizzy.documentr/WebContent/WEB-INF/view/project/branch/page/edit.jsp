<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${page.projectName}"/>"><spring:message code="title.projectX" arguments="${page.projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/branch/${page.projectName}/${page.branchName}"/>"><spring:message code="title.branchX" arguments="${page.branchName}"/></a> <span class="divider">/</span></li>
	<c:if test="${!empty page.path}">
		<li><a href="<c:url value="/page/${page.projectName}/${page.branchName}/${page.path}"/>"><spring:message code="title.pageX" arguments="${page.title}"/></a> <span class="divider">/</span></li>
	</c:if>
	<li class="active"><spring:message code="title.editPage"/></li>
</ul>

<div class="page-header"><h1><spring:message code="title.editPage"/></h1></div>

<p>
<c:set var="action"><c:url value="/page/save/${page.projectName}/${page.branchName}"/></c:set>
<form:form commandName="page" action="${action}" method="POST" cssClass="well">
	<fieldset>
		<form:label path="path"><spring:message code="label.path"/>:</form:label>
		<c:set var="disabled"><c:if test="${!empty page.path}">disabled</c:if></c:set>
		<form:input path="path" cssClass="input-xlarge ${disabled}" disabled="${!empty disabled}"/>
		<c:if test="${!empty disabled}">
			<form:hidden path="path"/>
		</c:if>
	</fieldset>
	<fieldset>
		<form:label path="title"><spring:message code="label.title"/>:</form:label>
		<form:input path="title" cssClass="input-xlarge"/>
	</fieldset>
	<fieldset>
		<form:label path="text"><spring:message code="label.contents"/>:</form:label>
		<form:textarea path="text" cssClass="span11" rows="20"/>
	</fieldset>
	<fieldset>
		<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
		<c:choose>
			<c:when test="${!empty page.path}">
				<c:set var="pathUrl" value="${fn:replace(page.path, '/', ',')}"/>
				<a href="<c:url value="/page/${page.projectName}/${page.branchName}/${pathUrl}"/>" class="btn"><spring:message code="button.cancel"/></a>
			</c:when>
			<c:otherwise>
				<a href="<c:url value="/branch/${page.projectName}/${page.branchName}"/>" class="btn"><spring:message code="button.cancel"/></a>
			</c:otherwise>
		</c:choose>
	</fieldset>
</form:form>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
