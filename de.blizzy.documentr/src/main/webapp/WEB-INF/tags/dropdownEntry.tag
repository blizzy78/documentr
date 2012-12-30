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
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ attribute name="divider" required="false" rtexprvalue="true" %>

<c:set var="body"><jsp:doBody/></c:set>
<c:set var="body" value="${fn:trim(body)}"/>

<c:if test="${divider and requestScope._dropdownNeedDivider and !empty body}">
	<li class="divider"></li>
</c:if>
<c:if test="${!empty body}">
	<c:out value="${body}" escapeXml="false"/>
	<c:set var="_dropdownNeedDivider" scope="request" value="${true}"/>
</c:if>
