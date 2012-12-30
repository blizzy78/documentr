<%--
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012-2013 Maik Schreiber

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ tag pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>

<jsp:include page="/WEB-INF/view/pageHeader.jsp"/>

<c:if test="${!empty requestScope._breadcrumbs}"><c:out value="${requestScope._breadcrumbs}" escapeXml="false"/></c:if>

<c:set var="siteNotice" value="${d:getSystemSetting('siteNotice')}"/>

<c:set var="pageContents"><%--
--%><c:if test="${!empty siteNotice}"><div class="alert alert-error alert-block site-notice"><h4><spring:message code="title.siteNotice"/></h4><c:out value="${siteNotice}"/></div></c:if><%--
--%><jsp:doBody/><%--
--%></c:set>

<c:if test="${!empty pageContents}">
	<div class="container container-main">
		<c:out value="${pageContents}" escapeXml="false"/>
		<c:set var="footer" value="${d:getSystemSetting('page.footerHtml')}"/>
		<c:if test="${!empty footer}"><c:out value="${footer}" escapeXml="false"/></c:if>
		<jsp:include page="/WEB-INF/view/footer.jsp"/>
	</div>
</c:if>

<jsp:include page="/WEB-INF/view/pageFooter.jsp"/>
