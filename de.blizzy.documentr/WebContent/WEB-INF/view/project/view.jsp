<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.projectX" arguments="${name}"/></li>
</ul>

<div class="page-header"><h1><spring:message code="title.branches"/></h1></div>

<p>
<c:set var="branches" value="${d:listProjectBranches(name)}"/>
<c:choose>
	<c:when test="${!empty branches}">
		<c:forEach var="branch" items="${branches}">
			<a href="<c:url value="/branch/${name}/${branch}"/>"><c:out value="${branch}"/></a><br/>
		</c:forEach>
	</c:when>
	<c:otherwise>No branches found.</c:otherwise>
</c:choose>
</p>

<p>
<a href="<c:url value="/branch/create/${name}"/>" class="btn"><spring:message code="button.createBranch"/></a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
