<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/WEB-INF/view/header.jsp"/>

<c:if test="${!empty requestScope._breadcrumbs}"><c:out value="${requestScope._breadcrumbs}" escapeXml="false"/></c:if>

<c:set var="pageContents"><jsp:doBody/></c:set>
<c:if test="${!empty pageContents}">
	<div class="container">
		<c:out value="${pageContents}" escapeXml="false"/>
	</div>
</c:if>

<jsp:include page="/WEB-INF/view/footer.jsp"/>
