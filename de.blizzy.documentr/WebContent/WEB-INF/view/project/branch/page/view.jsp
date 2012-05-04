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

<c:set var="attachments" value="${d:listPageAttachments(projectName, branchName, path)}"/>
<div class="btn-toolbar" style="float:right">
	<div class="btn-group">
		<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn" title="<spring:message code="button.editPage"/>"><i class="icon-edit"></i> <spring:message code="button.edit"/></a>
	</div>
	<div class="btn-group">
		<a class="btn dropdown-toggle" data-toggle="dropdown" href="javascript:;"><i class="icon-cog"></i> <spring:message code="button.tools"/> <span class="caret"></span></a>
		<ul class="dropdown-menu">
			<li><a href="<c:url value="/attachment/create/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-download-alt"></i> <spring:message code="button.addAttachment"/></a></li>
			<li><a href="<c:url value="/attachment/list/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>"><i class="icon-list"></i>
					<c:choose>
						<c:when test="${!empty attachments}"><spring:message code="button.attachmentsX" arguments="${fn:length(attachments)}"/></c:when>
						<c:otherwise><spring:message code="button.attachments"/></c:otherwise>
					</c:choose>
				</a></li>
			<li class="divider"></li>
			<li><a href="javascript:;"><i class="icon-file"></i> <spring:message code="button.addChildPage"/></a></li>
		</ul>
	</div>
</div>

<div class="page-header"><h1><c:out value="${title}"/></h1></div>

<c:out value="${d:markdownToHTML(text)}" escapeXml="false"/>

<p class="spacer">
<a href="<c:url value="/page/edit/${projectName}/${branchName}/${d:toURLPagePath(path)}"/>" class="btn"><i class="icon-edit"></i> <spring:message code="button.editPage"/></a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
