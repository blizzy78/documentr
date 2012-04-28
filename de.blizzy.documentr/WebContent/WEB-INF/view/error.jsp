<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<jsp:include page="/WEB-INF/view/header.jsp"/>

<div class="page-header"><h1>Error</h1></div>

<p>
<c:choose>
	<c:when test="${!empty messageKey}"><spring:message code="${messageKey}"/></c:when>
	<c:otherwise>
		<%
		request.setAttribute("statusCode", request.getAttribute("javax.servlet.error.status_code")); //$NON-NLS-1$ //$NON-NLS-2$
		%>
		<c:out value="Error ${statusCode}"/>
	</c:otherwise>
</c:choose>
</p>

<p>
<a href="javascript:void(history.go(-1))" class="btn">Go Back</a>
</p>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
