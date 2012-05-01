<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<ul class="breadcrumb">
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${projectName}"/>"><c:out value="${projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/branch/${projectName}/${branchName}"/>"><c:out value="${branchName}"/></a> <span class="divider">/</span></li>
	<li class="active"><c:out value="${title}"/></li>
</ul>

<div class="btn-toolbar">
	<div class="btn-group">
		<a class="btn dropdown-toggle" data-toggle="dropdown" href="javascript:;"><i class="icon-plus"></i> <spring:message code="button.add"/> <span class="caret"></span></a>
		<ul class="dropdown-menu">
			<li><a href="javascript:;"><i class="icon-download-alt"></i> <spring:message code="button.attachment"/></a></li>
			<li><a href="javascript:;"><i class="icon-file"></i> <spring:message code="button.childPage"/></a></li>
		</ul>
	</div>
</div>

<div class="page-header"><h1>
<c:out value="${title}"/>
<small>
<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn btn-mini" title="<spring:message code="button.editPage"/>"><i class="icon-edit"></i></a>
</small>
</h1></div>

<c:out value="${d:markdownToHTML(text)}" escapeXml="false"/>

<p class="spacer">
<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn"><i class="icon-edit"></i> <spring:message code="button.editPage"/></a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
