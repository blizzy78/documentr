/*
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
*/

define(function() {
	"use strict";
	
	$.fn.extend({
		showModal: function(options) {
			var modalBackdrop = (documentr.isSomething(options) && documentr.isSomething(options.backdrop)) ? options.backdrop : true;
			var modalKeyboard = (documentr.isSomething(options) && documentr.isSomething(options.keyboard)) ? options.keyboard : true;
			this.modal({
				backdrop: modalBackdrop,
				keyboard: modalKeyboard
			});
			var win = $(window);
			this.css('margin-left', '0')
				.css('margin-top', '0')
				.css('left', Math.floor((win.width() - this.width()) / 2) + 'px')
				.css('top', Math.floor((win.height() - this.height()) / 2) + 'px');
			return this;
		},
		
		hideModal: function() {
			this.modal('hide');
			return this;
		}
	});
	
	return {
		openMessageDialog: function(title, message, buttons, options) {
			var backdrop = (documentr.isSomething(options) && documentr.isSomething(options.backdrop)) ? options.backdrop : true;
			var keyboard = (documentr.isSomething(options) && documentr.isSomething(options.keyboard)) ? options.keyboard : true;
			var wide = (documentr.isSomething(options) && documentr.isSomething(options.wide)) ? options.wide : false;
			var messageAsHtml = (documentr.isSomething(options) && documentr.isSomething(options.messageAsHtml)) ? options.messageAsHtml : false;
			
			var id = "dialog_" + new Date().getTime();
			var html =
				'<div class="modal" id="' + id + '" style="display: none;">' +
				'<div class="modal-header">' +
				'<button class="close" id="' + id + '_close">&#x00D7</button>' +
				'<h3 id="' + id + '_title">title</h3>' +
				'</div>' +
				'<div class="modal-body" id="' + id + '_body"></div>' +
				'<div class="modal-footer" id="' + id + '_footer"></div>' +
				'</div>';
			var dlgEl = $(html);
			if (wide) {
				dlgEl.addClass('modal-wide');
			}
			
			var footerEl = dlgEl.find('#' + id + '_footer');
			
			function close() {
				dlgEl.hideModal();
				dlgEl.remove();
			}
			
			function setText(message) {
				if (messageAsHtml) {
					dlgEl.find('#' + id + '_body').html(message);
				} else {
					dlgEl.find('#' + id + '_body').text(message);
				}
			}
			
			function setAllButtonsDisabled() {
				footerEl.find('a').off('click').setButtonDisabled(true);
			}
			
			var dlg = {
					close: close,
					setText: setText,
					setAllButtonsDisabled: setAllButtonsDisabled
			};
			
			dlgEl.find('#' + id + '_title').text(title);
			setText(message);
			dlgEl.find('#' + id + '_close').click(close);
			var clickHandler = function(event) {
				var buttonOptions = event.data;
				var closeDlg = documentr.isSomething(buttonOptions.close) && buttonOptions.close;
				var cancelDlg = documentr.isSomething(buttonOptions.cancel) && buttonOptions.cancel;
				if (cancelDlg) {
					closeDlg = true;
				}
				
				if (closeDlg) {
					close();
				}
				if (!cancelDlg) {
					buttonOptions.onclick.call(dlg);
				}
				event.preventDefault();
			};
			for (var i = 0; i < buttons.length; i++) {
				var button = buttons[i];
				var b = $('<a href="#" class="btn"></a>');
				if (documentr.isSomething(button.onclick) ||
						(documentr.isSomething(button.cancel) && button.cancel)) {
					
					b.click(button, clickHandler);
				} else if (documentr.isSomething(button.href)) {
					b.attr('href', button.href);
				}
				b.text(button.text);
				if (documentr.isSomething(button.type)) {
					b.addClass('btn-' + button.type);
				}
				footerEl.append(b);
			}
			$(document.body).append(dlgEl);
			
			dlgEl.showModal({
				backdrop: backdrop,
				keyboard: keyboard
			});
			
			return dlg;
		}
	};
});
