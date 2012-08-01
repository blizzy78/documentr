<%--
documentr - Edit, maintain, and present software documentation on the web.
Copyright (C) 2012 Maik Schreiber

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
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="d" uri="http://documentr.org/tld/documentr" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags" %>

<sec:authorize access="(#pageForm.path == null) ?
	hasBranchPermission(#pageForm.projectName, #pageForm.branchName, EDIT_PAGE) :
	hasPagePermission(#pageForm.projectName, #pageForm.branchName, #pageForm.path, EDIT_PAGE)">

<c:choose>
	<c:when test="${!empty pageForm.path}"><c:set var="hierarchyPagePath" value="${pageForm.path}"/></c:when>
	<c:when test="${!empty pageForm.parentPagePath}"><c:set var="hierarchyPagePath" value="${pageForm.parentPagePath}"/></c:when>
	<c:otherwise><c:set var="hierarchyPagePath" value="home"/></c:otherwise>
</c:choose>

<dt:headerJS>

<c:if test="${empty pageForm.path}">
$(function() {
	var el = $('#pageForm').find('#title');
	el.blur(function() {
		var pinPathButton = $('#pinPathButton');
		if (!pinPathButton.hasClass('active')) {
			var fieldset = $('#pathFieldset');
			fieldset.removeClass('warning').removeClass('error');
			$('#pathExistsWarning').remove();
	
			var value = el.val();
			if (value.length > 0) {
				$.ajax({
					url: '<c:url value="/page/generateName/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(hierarchyPagePath)}/json"/>',
					type: 'POST',
					dataType: 'json',
					data: {
						title: value
					},
					success: function(result) {
						$('#pageForm').find('#path').val(result.path);
						if (result.exists) {
							fieldset.addClass('warning');
							fieldset.append($('<span id="pathExistsWarning" class="help-inline">' +
								'<spring:message code="page.path.exists"/></span>'));
						}
						$('#pinPathButton').removeClass('disabled').removeClass('active');
					}
				});
			}
		}
	});
});
</c:if>

function togglePreview() {
	var previewEl = $('#preview');
	if (previewEl.length === 0) {
		var textEl = $('#pageForm').find('#text');
		$.ajax({
			url: '<c:url value="/page/markdownToHTML/${pageForm.projectName}/${pageForm.branchName}/json"/>',
			type: 'POST',
			dataType: 'json',
			data: {
				pagePath: '<c:out value="${hierarchyPagePath}"/>',
				markdown: textEl.val()
			},
			success: function(result) {
				$('#textEditorToolbar a').each(function() {
					$(this).setButtonDisabled(true);
				});
				$('#togglePreviewButton').setButtonDisabled(false);

				previewEl = $('<div id="preview" class="preview"></div>');
				previewEl.html(result.html);
				$(document.body).append(previewEl);
				prettyPrint();
				previewEl
					.css('left', textEl.offset().left)
					.css('top', textEl.offset().top)
					.css('width', textEl.outerWidth() - (previewEl.outerWidth() - previewEl.width()))
					.css('height', textEl.outerHeight() - (previewEl.outerHeight() - previewEl.height()))
					.slideToggle('fast');
			}
		});
	} else {
		previewEl.slideToggle('fast', function() {
			previewEl.remove();
		});

		$('#textEditorToolbar a').each(function() {
			$(this).setButtonDisabled(false);
		});
	}
}

function toggleStyleBold() {
	var textEl = $('#text');
	var start = textEl[0].selectionStart;
	var end = textEl[0].selectionEnd;
	var text = textEl.val();
	var isBold = (start >= 2) && (end <= (text.length - 2)) &&
		(text.substring(start - 2, start) === '**') &&
		(text.substring(end, end + 2) === '**');
	if (!isBold) {
		text = text.substring(0, start) + '**' + text.substring(start, end) + '**' + text.substring(end);
		start = start + 2;
		end = end + 2;
	} else {
		text = text.substring(0, start - 2) + text.substring(start, end) + text.substring(end + 2);
		start = start - 2;
		end = end - 2;
	}
	textEl.val(text);
	textEl[0].setSelectionRange(start, end);
	textEl.focus();
}

function toggleStyleItalic() {
	var textEl = $('#text');
	var start = textEl[0].selectionStart;
	var end = textEl[0].selectionEnd;
	var text = textEl.val();
	var isItalic = (start >= 1) && (end <= (text.length - 1)) &&
		(text.substring(start - 1, start) === '*') &&
		(text.substring(end, end + 1) === '*');
	if (!isItalic) {
		text = text.substring(0, start) + '*' + text.substring(start, end) + '*' + text.substring(end);
		start++;
		end++;
	} else {
		text = text.substring(0, start - 1) + text.substring(start, end) + text.substring(end + 1);
		start--;
		end--;
	}
	textEl.val(text);
	textEl[0].setSelectionRange(start, end);
	textEl.focus();
}

function insertMacro(insertText) {
	if (insertText.indexOf('[') < 0) {
		insertText = insertText + '[]';
	}
	var selectionStart = insertText.indexOf('[');
	var selectionEnd = insertText.indexOf(']') - 1;
	insertText = insertText.replace(/\[/, '').replace(/\]/, '');
	var textEl = $('#text');
	var end = textEl[0].selectionEnd;
	var text = textEl.val();
	text = text.substring(0, end) + insertText + text.substring(end);
	textEl.val(text);
	textEl[0].setSelectionRange(end + selectionStart, end + selectionEnd);
	textEl.focus();
}

function toggleFullscreen() {
	$('#titleFieldset, #pathFieldset, #textLabel, #viewRestrictionRoleFieldset').toggle();
	var textEl = $('#text');
	var rows = textEl.attr('rows');
	textEl.attr('rows', (rows == 29) ? '20' : '29');
}

function showMarkdownHelp() {
	window.open('<c:url value="/help/markdown"/>', 'documentrMarkdownHelp',
		'width=700, height=600, dependent=yes, location=no, menubar=no, resizable=yes, scrollbars=yes, status=no, toolbar=no');
}

function updateInsertLinkButton() {
	var linkedPage = $('#insert-link-dialog').data('linkedPage');
	var url = $('#insert-link-dialog input[name="url"]').val();
	var internal = $('#insert-link-dialog .nav-tabs li:eq(0)').hasClass('active');
	var valid = (internal && documentr.isSomething(linkedPage)) ||
		(!internal && (url.length > 0));
	$('#insert-link-button').setButtonDisabled(!valid);
}

function openInsertLinkDialog() {
	var textEl = $('#text');
	var start = textEl[0].selectionStart;
	var end = textEl[0].selectionEnd;
	var text = textEl.val();
	var linkText = text.substring(start, end);
	$('#insert-link-linktext').val(linkText);
	$('#insert-link-dialog input[name="url"]').val('');
	$('#insert-link-dialog input[name="externalLinkText"]').val(linkText);
	$('#insert-link-dialog .nav-tabs a:first').tab('show');

	function showDialog() {
		updateInsertLinkButton();
		$('#insert-link-dialog').showModal();
	}

	var treeEl = $('#linked-page-tree');
	if (treeEl.children().length === 0) {
		documentr.createPageTree(treeEl, {
				start: {
					type: 'application'
				},
				selectable: {
					projects: false,
					branches: false
				},
				checkBranchPermissions: 'VIEW',
				showAttachments: true
			})
			.bind('loaded.jstree', function() {
				showDialog();
			})
			.bind('select_node.jstree', function(event, data) {
				var node = data.rslt.obj;
				if (node.data('type') === 'page') {
					var linkedPage = {
						path: node.data('projectName') + '/' + node.data('branchName') + '/' +
							node.data('path').replace(/\//g, ','),
						pageType: 'page'
					};
					$('#insert-link-dialog').data('linkedPage', linkedPage);
				} else if (node.data('type') === 'attachment') {
					var linkedPage = {
						path: node.data('projectName') + '/' + node.data('branchName') + '/' +
							node.data('pagePath').replace(/\//g, ',') + '/' + node.data('name'),
						pageType: 'attachment'
					};
					$('#insert-link-dialog').data('linkedPage', linkedPage);
				} else {
					$('#insert-link-dialog').data('linkedPagePath', null);
				}
				updateInsertLinkButton();
			})
			.bind('deselect_node.jstree', function() {
				$('#insert-link-dialog').data('linkedPagePath', null);
				updateInsertLinkButton();
			});
	} else {
		showDialog();
	}
}

function insertLink() {
	$('#insert-link-dialog').hideModal();

	var internal = $('#insert-link-dialog .nav-tabs li:eq(0)').hasClass('active');
	var linkedPage = $('#insert-link-dialog').data('linkedPage');
	var linkText = internal ? $('#insert-link-linktext').val() : $('#insert-link-dialog input[name="externalLinkText"]').val();
	var textEl = $('#text');
	var start = textEl[0].selectionStart;
	var end = textEl[0].selectionEnd;
	var link;
	if (internal) {
		if (linkedPage.pageType === 'page') {
			link = '<c:url value="/page/"/>' + linkedPage.path;
		} else {
			link = '<c:url value="/attachment/"/>' + linkedPage.path;
		}
	} else {
		link = $('#insert-link-dialog input[name="url"]').val();
	}
	var text = textEl.val();
	text = text.substring(0, start) + '[[' + link + ' ' + linkText + ']]' + text.substring(end);
	start = start + link.length + 3;
	end = start + linkText.length;
	textEl.val(text);
	textEl[0].setSelectionRange(start, end);
	textEl.focus();
}

function updateInsertImageButton() {
	var linkedImage = $('#insert-image-dialog').data('linkedImage');
	var altText = $('#insert-image-alttext').val();
	$('#insert-image-button').setButtonDisabled(!documentr.isSomething(linkedImage) || (altText.length === 0));
}

function openInsertImageDialog() {
	function showDialog() {
		$('#insert-image-alttext').val('');
		updateInsertImageButton();
		$('#insert-image-dialog').showModal();
	}

	var treeEl = $('#linked-image-tree');
	if (treeEl.children().length === 0) {
		documentr.createPageTree(treeEl, {
				start: {
					type: 'page',
					projectName: '<c:out value="${projectName}"/>',
					branchName: '<c:out value="${branchName}"/>',
					pagePath: '<c:out value="${path}"/>'
				},
				checkBranchPermissions: 'VIEW',
				showPages: false,
				showAttachments: true
			})
			.bind('loaded.jstree', function() {
				showDialog();
			})
			.bind('select_node.jstree', function(event, data) {
				var node = data.rslt.obj;
				var linkedImage = node.data('name');
				$('#insert-image-dialog').data('linkedImage', linkedImage);
				updateInsertImageButton();
			})
			.bind('deselect_node.jstree', function() {
				$('#insert-image-dialog').data('linkedImage', null);
				updateInsertImageButton();
			});
	} else {
		showDialog();
	}
}

function insertImage() {
	$('#insert-image-dialog').hideModal();
	
	var linkedImage = $('#insert-image-dialog').data('linkedImage');
	var altText = $('#insert-image-alttext').val();
	var thumbnail = $('#insert-image-thumbnail:checked').length === 1;
	if (thumbnail) {
		linkedImage = linkedImage + ' | thumb';
	}
	var textEl = $('#text');
	var start = textEl[0].selectionStart;
	var text = textEl.val();
	text = text.substring(0, start) + '![' + altText + '](' + linkedImage + ')' + text.substring(start);
	start = start + 2;
	var end = start + altText.length;
	textEl.val(text);
	textEl[0].setSelectionRange(start, end);
	textEl.focus();
}

$(function() {
	$('#insert-link-dialog .nav-tabs a').click(function(e) {
		e.preventDefault();
		$(this).tab('show');
		updateInsertLinkButton();
	});
});

</dt:headerJS>

<dt:breadcrumbs>
	<li><a href="<c:url value="/projects"/>"><spring:message code="title.projects"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/project/${pageForm.projectName}"/>"><c:out value="${pageForm.projectName}"/></a> <span class="divider">/</span></li>
	<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/home"/>"><c:out value="${pageForm.branchName}"/></a> <span class="divider">/</span></li>
	<c:set var="hierarchy" value="${d:getPagePathHierarchy(pageForm.projectName, pageForm.branchName, hierarchyPagePath)}"/>
	<c:forEach var="entry" items="${hierarchy}" varStatus="status">
		<c:if test="${!status.first}">
			<li><a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(entry)}"/>"><c:out value="${d:getPageTitle(pageForm.projectName, pageForm.branchName, entry)}"/></a> <span class="divider">/</span></li>
		</c:if>
	</c:forEach>
	<li class="active"><spring:message code="title.editPage"/></li>
</dt:breadcrumbs>

<dt:pageTitle><spring:message code="title.editPage"/></dt:pageTitle>

<dt:page>

<div class="page-header"><h1><spring:message code="title.editPage"/></h1></div>

<p>
<c:set var="action"><c:url value="/page/save/${pageForm.projectName}/${pageForm.branchName}"/></c:set>
<form:form commandName="pageForm" action="${action}" method="POST" cssClass="well form-horizontal">
	<fieldset>
		<form:hidden path="parentPagePath"/>
		<form:hidden path="commit"/>
		<c:set var="errorText"><form:errors path="title"/></c:set>
		<div id="titleFieldset" class="control-group <c:if test="${!empty errorText}">error</c:if>">
			<form:label path="title" cssClass="control-label"><spring:message code="label.title"/>:</form:label>
			<form:input path="title" cssClass="input-xlarge"/>
			<c:if test="${!empty errorText}"><span class="help-inline"><c:out value="${errorText}" escapeXml="false"/></span></c:if>
		</div>
		<div id="pathFieldset" class="control-group">
			<form:hidden path="path"/>
			<form:label path="path" cssClass="control-label"><spring:message code="label.pathGeneratedAutomatically"/>:</form:label>
			<form:input path="path" cssClass="input-xlarge disabled" disabled="true"/>
			<c:if test="${empty pageForm.path}">
				<a id="pinPathButton" class="btn disabled" data-toggle="button" href="javascript:;" title="<spring:message code="button.pinPath"/>"><i class="icon-lock"></i></a>
			</c:if>
		</div>
		<div class="control-group">
			<form:label id="textLabel" path="text" cssClass="control-label"><spring:message code="label.contents"/>:</form:label>
			<div id="textEditor" class="texteditor">
				<div id="textEditorToolbar" class="btn-toolbar btn-toolbar-icons">
					<div class="btn-group">
						<a id="togglePreviewButton" href="javascript:togglePreview();" class="btn" data-toggle="button" title="<spring:message code="button.showPreview"/>"><i class="icon-eye-open"></i></a>
						<a href="javascript:toggleFullscreen();" class="btn" data-toggle="button" title="<spring:message code="button.zoomEditor"/>"><i class="icon-fullscreen"></i></a>
					</div>
					<div class="btn-group">
						<a href="javascript:toggleStyleBold();" class="btn" title="<spring:message code="button.bold"/>"><i class="icon-bold"></i></a>
						<a href="javascript:toggleStyleItalic();" class="btn" title="<spring:message code="button.italic"/>"><i class="icon-italic"></i></a>
					</div>
					<div class="btn-group">
						<a href="javascript:openInsertImageDialog();" class="btn" title="<spring:message code="button.insertImage"/>"><i class="icon-picture"></i></a>
					</div>
					<div class="btn-group">
						<a href="javascript:openInsertLinkDialog();" class="btn" title="<spring:message code="button.insertLink"/>"><i class="icon-share-alt"></i></a>
					</div>
					<div class="btn-group">
						<a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><spring:message code="button.macros"/> <span class="caret"></span></a>
						<ul class="dropdown-menu">
							<c:set var="macros" value="${d:getMacros()}"/>
							<c:forEach var="macro" items="${macros}">
								<dt:dropdownEntry>
									<li><a href="javascript:insertMacro('<c:out value="${macro.insertText}"/>');"><spring:message code="${macro.titleKey}"/> <div class="macro-description"><spring:message code="${macro.descriptionKey}"/></div></a></li>
								</dt:dropdownEntry>
							</c:forEach>
						</ul>
					</div>
					<div class="btn-group">
						<a href="javascript:showMarkdownHelp();" class="btn" title="<spring:message code="button.showFormattingHelp"/>"><i class="icon-question-sign"></i></a>
					</div>
				</div>
				<form:textarea id="text" path="text" cssClass="span11 code" rows="20"/>
			</div>
		</div>
		<div id="viewRestrictionRoleFieldset" class="control-group">
			<form:label path="viewRestrictionRole" cssClass="control-label"><spring:message code="label.visibleForRole"/>:</form:label>
			<form:select path="viewRestrictionRole">
				<form:option value="">(<spring:message code="everyone"/>)</form:option>
				<c:set var="roles" value="${d:listRoles()}"/>
				<form:options items="${roles}"/>
			</form:select>
		</div>
		<div class="form-actions">
			<input type="submit" class="btn btn-primary" value="<spring:message code="button.save"/>"/>
			<a href="<c:url value="/page/${pageForm.projectName}/${pageForm.branchName}/${d:toURLPagePath(hierarchyPagePath)}"/>" class="btn"><spring:message code="button.cancel"/></a>
		</div>
	</fieldset>
</form:form>
</p>

<div class="modal" id="insert-link-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#insert-link-dialog').modal('hide');">×</button>
		<h3><spring:message code="title.insertLink"/></h3>
	</div>
	<div class="modal-body">
		<form id="insertLinkForm" action="" method="POST" class="form-horizontal">
			<ul class="nav nav-tabs">
				<li class="active"><a href="#insert-link-internal"><spring:message code="title.pageOrAttachment"/></a></li>
				<li><a href="#insert-link-external"><spring:message code="title.externalWebPage"/></a></li>
			</ul>

			<div class="tab-content">
				<fieldset id="insert-link-internal" class="tab-pane active">
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkedPage"/>:</label>
						<div class="controls">
							<div id="linked-page-tree"></div>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkText"/>:</label>
						<div class="controls">
							<input type="text" id="insert-link-linktext" class="input-xlarge"/>
						</div>
					</div>
				</fieldset>
				<fieldset id="insert-link-external" class="tab-pane">
					<div class="control-group">
						<label class="control-label"><spring:message code="label.url"/>:</label>
						<div class="controls">
							<input type="text" name="url" class="input-xlarge" onkeyup="updateInsertLinkButton()"/>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label"><spring:message code="label.linkText"/>:</label>
						<div class="controls">
							<input type="text" name="externalLinkText" class="input-xlarge"/>
						</div>
					</div>
				</fieldset>
			</div>
		</form>
	</div>
	<div class="modal-footer">
		<a id="insert-link-button" href="javascript:void(insertLink());" class="btn btn-primary"><spring:message code="button.insertLink"/></a>
		<a href="javascript:void($('#insert-link-dialog').modal('hide'));" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

<div class="modal" id="insert-image-dialog" style="display: none;">
	<div class="modal-header">
		<button class="close" onclick="$('#insert-image-dialog').modal('hide');">×</button>
		<h3><spring:message code="title.insertImage"/></h3>
	</div>
	<div class="modal-body">
		<form action="" method="POST" class="form-horizontal">
			<fieldset>
				<div class="control-group">
					<label class="control-label"><spring:message code="label.image"/>:</label>
					<div class="controls">
						<div id="linked-image-tree"></div>
					</div>
				</div>
				<div class="control-group">
					<label class="control-label"><spring:message code="label.altText"/>:</label>
					<div class="controls">
						<input type="text" id="insert-image-alttext" class="input-xlarge" onkeyup="updateInsertImageButton()"/>
					</div>
				</div>
				<div class="control-group">
					<label class="checkbox">
						<input type="checkbox" id="insert-image-thumbnail">
						<spring:message code="label.insertAsThumbnail"/>
					</label>
				</div>
			</fieldset>
		</form>
	</div>
	<div class="modal-footer">
		<a id="insert-image-button" href="javascript:void(insertImage());" class="btn btn-primary"><spring:message code="button.insertImage"/></a>
		<a href="javascript:void($('#insert-image-dialog').modal('hide'));" class="btn"><spring:message code="button.cancel"/></a>
	</div>
</div>

</dt:page>

</sec:authorize>
