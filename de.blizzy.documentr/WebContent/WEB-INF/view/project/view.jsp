<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li class="active"><c:out value="${name}"/></li>
</ul>

<div class="page-header"><h1><spring:message code="title.projectX" arguments="${name}"/></h1></div>

<h2><spring:message code="title.branches"/></h2>

<c:set var="branches" value="${d:listProjectBranches(name)}"/>
<c:choose>
	<c:when test="${!empty branches}">
		<ul>
		<c:forEach var="branch" items="${branches}">
			<li><a href="<c:url value="/branch/${name}/${branch}"/>"><c:out value="${branch}"/></a></li>
		</c:forEach>
		</ul>
	</c:when>
	<c:otherwise><p>No branches found.</p></c:otherwise>
</c:choose>

<p>
<a href="<c:url value="/branch/create/${name}"/>" class="btn"><i class="icon-plus"></i> <spring:message code="button.createBranch"/></a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
