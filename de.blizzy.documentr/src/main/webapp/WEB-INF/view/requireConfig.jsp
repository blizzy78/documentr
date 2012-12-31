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
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<script>
var require = {
	baseUrl: '<c:url value="/js"/>',
	map: {
		'*': {
			diff_match_patch: 'diff_match_patch-20120106',
			'jquery.fileupload': 'jquery-file-upload-20121221/jquery.fileupload',
			'jquery.ui.widget': 'jquery-file-upload-20121221/vendor/jquery.ui.widget',
			slimbox: 'slimbox-2.04',
			'jquery.jstree': 'jquery.jstree-pre-1.0-fix-2/jquery.jstree',
			ace: 'ace-20121217/ace',
			zxcvbn: 'zxcvbn-20120416',
			'jquery.select2': 'select2-3.2.min'
		}
	},
	shim: {
		'diff_match_patch-20120106': {
			exports: 'diff_match_patch'
		},
		'ace-20121217/ace': {
			exports: 'ace'
		},
		'zxcvbn-20120416': {
			exports: 'zxcvbn'
		}
	}<sec:authorize access="isAuthenticated()">,
	config: {
		'documentr/pageTree': {
			applicationUrl: '<c:url value="/pageTree/application/json"/>',
			projectUrl: '<c:url value="/pageTree/project/_PROJECTNAME_/json"/>',
			branchUrl: '<c:url value="/pageTree/branch/_PROJECTNAME_/_BRANCHNAME_/json"/>',
			pageUrl: '<c:url value="/pageTree/page/_PROJECTNAME_/_BRANCHNAME_/_PAGEPATH_/json"/>',
			projectTitle: "<spring:message code="label.projectX" arguments="_PROJECTNAME_"/>",
			branchTitle: "<spring:message code="label.branchX" arguments="_BRANCHNAME_"/>",
			iconUrls: {
				project: '<c:url value="/img/project.png"/>',
				branch: '<c:url value="/img/branch.png"/>',
				page: '<c:url value="/img/page.png"/>',
				attachment: '<c:url value="/img/attachment.png"/>'
			}
		}
	}</sec:authorize>
};
</script>
