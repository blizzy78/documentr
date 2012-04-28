<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><spring:message code="title.projectX" arguments="${projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/branch/${projectName}/${branchName}"/>"><spring:message code="title.branchX" arguments="${branchName}"/></a> <span class="divider">/</span></li>
	<li class="active"><spring:message code="title.pageX" arguments="${title}"/></li>
</ul>

<div class="page-header"><h1><c:out value="${title}"/></h1></div>

<p>
<c:out value="${text}" escapeXml="false"/>
</p>

<p>
<a href="<c:url value="/page/edit/${projectName}/${branchName}/${fn:replace(path, '/', ',')}"/>" class="btn"><spring:message code="button.editPage"/></a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
