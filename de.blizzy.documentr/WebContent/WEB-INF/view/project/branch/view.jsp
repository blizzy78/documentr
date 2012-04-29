<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li class="active"><c:out value="${name}"/></li>
</ul>

<div class="page-header"><h1><spring:message code="title.branchX" arguments="${name}"/></h1></div>

<h2><spring:message code="title.pages"/></h2>

<p>
<c:set var="pagePaths" value="${d:listPagePaths(projectName, name)}"/>
<c:choose>
	<c:when test="${!empty pagePaths}">
		<c:forEach var="path" items="${pagePaths}">
			<c:set var="pathUrl" value="${d:toURLPagePath(path)}"/>
			<a href="<c:url value="/page/${projectName}/${name}/${pathUrl}"/>"><c:out value="${path}"/></a>
			<c:if test="${d:isPageSharedWithOtherBranches(projectName, name, path)}"> <span class="shared-page">(<spring:message code="shared"/>)</span></c:if>
			<br/>
		</c:forEach>
	</c:when>
	<c:otherwise>No pages found.</c:otherwise>
</c:choose>
</p>

<p>
<a href="<c:url value="/page/create/${projectName}/${name}"/>" class="btn"><spring:message code="button.createPage"/></a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
