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
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<c:if test="${searchResult.totalHits gt searchResult.hitsPerPage}">
	<c:set var="excess" value="${searchResult.totalHits mod searchResult.hitsPerPage}"/>
	<c:set var="numPages" value="${d:floor((searchResult.totalHits - excess) / searchResult.hitsPerPage)}"/>
	<c:if test="${excess gt 0}"><c:set var="numPages" value="${numPages + 1}"/></c:if>
	<c:set var="firstPage" value="${page - 2}"/>
	<c:if test="${firstPage lt 1}"><c:set var="firstPage" value="1"/></c:if>
	<c:set var="lastPage" value="${firstPage + 4}"/>
	<c:if test="${lastPage gt numPages}">
		<c:set var="lastPage" value="${numPages}"/>
		<c:set var="firstPage" value="${lastPage - 4}"/>
		<c:if test="${firstPage lt 1}"><c:set var="firstPage" value="1"/></c:if>
	</c:if>
	
	<dt:headerHTML>
		<c:if test="${page gt 1}">
			<link rel="prev" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${page - 1}"/></c:url>"/>
		</c:if>
		<c:if test="${page lt numPages}">
			<link rel="next" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${page + 1}"/></c:url>"/>
		</c:if>
	</dt:headerHTML>
</c:if>

<dt:breadcrumbs showSiteSearch="${searchText}">
	<li class="active"><spring:message code="title.searchResults"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.searchResults"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.searchResults"/></h1></div>

<c:if test="${!empty searchResult.suggestion}">
	<p class="search-didyoumean">
		<span class="leader"><spring:message code="label.didYouMean"/>:</span>
		<a href="<c:url value="/search/page"><c:param name="q" value="${searchResult.suggestion.searchText}"/></c:url>"><c:out value="${searchResult.suggestion.searchTextHtml}" escapeXml="false"/></a>
		<span class="hits"><spring:message code="xHits" arguments="${searchResult.suggestion.totalHits}"/></span>
	</p>
</c:if>

<c:choose>
	<c:when test="${searchResult.totalHits gt 0}">
		<c:forEach var="hit" items="${searchResult.hits}">
			<p class="search-hit">
				<a rel="nofollow" class="title" href="<c:url value="/page/${hit.projectName}/${hit.branchName}/${d:toUrlPagePath(hit.path)}"/>"><c:out value="${hit.title}"/></a>
				<c:forEach var="tag" items="${hit.tags}"><%--
					--%><span class="page-tag small"><c:out value="${tag}"/></span><%--
				--%></c:forEach>
				<br/>
				<a rel="nofollow" class="pagePath" href="<c:url value="/page/${hit.projectName}/${hit.branchName}/${d:toUrlPagePath(hit.path)}"/>"><c:out value="${hit.projectName}/${hit.branchName}/${d:toUrlPagePath(hit.path)}"/></a><br/>
				<c:out value="${hit.textHtml}" escapeXml="false"/>
			</p>
		</c:forEach>
		
		<c:if test="${searchResult.totalHits gt searchResult.hitsPerPage}">
			<c:set var="excess" value="${searchResult.totalHits mod searchResult.hitsPerPage}"/>
			<c:set var="numPages" value="${d:floor((searchResult.totalHits - excess) / searchResult.hitsPerPage)}"/>
			<c:if test="${excess gt 0}"><c:set var="numPages" value="${numPages + 1}"/></c:if>
			<c:set var="firstPage" value="${page - 2}"/>
			<c:if test="${firstPage lt 1}"><c:set var="firstPage" value="1"/></c:if>
			<c:set var="lastPage" value="${firstPage + 4}"/>
			<c:if test="${lastPage gt numPages}">
				<c:set var="lastPage" value="${numPages}"/>
				<c:set var="firstPage" value="${lastPage - 4}"/>
				<c:if test="${firstPage lt 1}"><c:set var="firstPage" value="1"/></c:if>
			</c:if>
			
			<p class="spacer">
				<div class="btn-toolbar">
					<div class="btn-group">
						<c:choose>
							<c:when test="${page gt 1}">
								<a rel="nofollow" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/></c:url>" class="btn"><i class="icon-fast-backward"></i></a>
								<a rel="nofollow" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${page - 1}"/></c:url>" class="btn"><i class="icon-backward"></i></a>
							</c:when>
							<c:otherwise>
								<a rel="nofollow" href="javascript:;" class="btn disabled"><i class="icon-fast-backward"></i></a>
								<a rel="nofollow" href="javascript:;" class="btn disabled"><i class="icon-backward"></i></a>
							</c:otherwise>
						</c:choose>
					</div>
					<div class="btn-group">
						<c:forEach begin="${firstPage}" end="${lastPage}" varStatus="status">
							<c:choose>
								<c:when test="${status.index eq page}">
									<c:set var="cssActive" value="active"/>
								</c:when>
								<c:otherwise>
									<c:set var="cssActive" value=""/>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${(status.index eq firstPage) and (firstPage gt 1)}">
									<a rel="nofollow" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${status.index}"/></c:url>" class="btn ${cssActive}">... ${status.index}</a>
								</c:when>
								<c:when test="${(status.index eq lastPage) and (lastPage lt numPages)}">
									<a rel="nofollow" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${status.index}"/></c:url>" class="btn ${cssActive}">${status.index} ...</a>
								</c:when>
								<c:otherwise>
									<a rel="nofollow" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${status.index}"/></c:url>" class="btn ${cssActive}">${status.index}</a>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</div>
					<div class="btn-group">
						<c:choose>
							<c:when test="${page lt numPages}">
								<a rel="nofollow" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${page + 1}"/></c:url>" class="btn"><i class="icon-forward"></i></a>
								<a rel="nofollow" href="<c:url value="/search/page"><c:param name="q" value="${searchText}"/><c:param name="p" value="${numPages}"/></c:url>" class="btn"><i class="icon-fast-forward"></i></a>
							</c:when>
							<c:otherwise>
								<a rel="nofollow" href="javascript:;" class="btn disabled"><i class="icon-forward"></i></a>
								<a rel="nofollow" href="javascript:;" class="btn disabled"><i class="icon-fast-forward"></i></a>
							</c:otherwise>
						</c:choose>

					</div>
				</div>
			</p>
		</c:if>
	</c:when>
	<c:otherwise>
		<p>
		<spring:message code="noDocumentsFoundForSearchTermsX" arguments="${searchText}" argumentSeparator="__DUMMY__"/>
		</p>
	</c:otherwise>
</c:choose>

</dt:page>
