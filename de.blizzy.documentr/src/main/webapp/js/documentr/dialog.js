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

define(['module'], function(module) {
	"use strict";
	
	var defaultModuleOptions = {
		cancelText: 'Cancel',
		closeText: 'Close'
	};
	var effectiveModuleOptions = $.extend({}, defaultModuleOptions, module.config());
	
	var centerEl = function(el) {
		var win = $(window);
		el.css({
			'margin-left': 0,
			'margin-top': 0,
			left: Math.floor((win.width() - el.width()) / 2) + 'px',
			top: Math.floor((win.height() - el.height()) / 2) + 'px'
		});
	};

	$.fn.extend({
		showModal: function(options) {
			var defaultOptions = {
				backdrop: true,
				keyboard: true
			};
			var effectiveOptions = $.extend({}, defaultOptions, options);
			this.modal({
				backdrop: effectiveOptions.backdrop,
				keyboard: effectiveOptions.keyboard
			});
			centerEl(this);
			return this;
		},
		
		hideModal: function() {
			this.modal('hide');
			return this;
		}
	});
	
	
	var buttonClickHandler = function(event) {
		var data = event.data;
		if (data.internal.close) {
			data.dialog.close();
		}
		if (documentr.isSomething(data.internal.href)) {
			window.location.href = data.internal.href;
		} else if (documentr.isSomething(data.internal.click)) {
			data.internal.click(data.button, data.dialog);
		}
	};

	var DialogButton = function() {
		var internal = {
			text: '',
			close: false,
			href: null,
			click: null,
			type: null,
			disabled: false,
			el: null
		};
		
		$.extend(this, {
			text: function(text) {
				internal.text = text;
				if (documentr.isSomething(internal.el)) {
					$(internal.el).text(text);
				}
				return this;
			},
			
			close: function(close) {
				internal.close = close;
				if (close) {
					this.text(effectiveModuleOptions.closeText);
				}
				return this;
			},
			
			cancel: function() {
				internal.close = true;
				this.text(effectiveModuleOptions.cancelText);
				return this;
			},
			
			href: function(href) {
				internal.href = href;
				internal.click = null;
				return this;
			},
			
			click: function(handler) {
				internal.click = handler;
				internal.href = null;
				return this;
			},
			
			type: function(type) {
				internal.type = type;
				return this;
			},
			
			primary: function() {
				this.type('primary');
				return this;
			},
			
			danger: function() {
				this.type('danger');
				return this;
			},
			
			disabled: function(disabled) {
				internal.disabled = disabled;
				if (documentr.isSomething(internal.el)) {
					$(internal.el).setButtonDisabled(disabled);
				}
				return this;
			},
			
			createElement: function(dialog) {
				internal.el = $.parseHTML('<button class="btn"></button>');
				$(internal.el).text(internal.text);
				if (documentr.isSomething(internal.type)) {
					$(internal.el).addClass('btn-' + internal.type);
				}
				var data = {
					button: this,
					internal: internal,
					dialog: dialog
				};
				$(internal.el).click(data, buttonClickHandler);
				if (internal.disabled) {
					$(internal.el).setButtonDisabled(true);
				}
				return internal.el;
			}
		});
	};
	
	var Dialog = function(options) {
		var defaultOptions = {
			backdrop: true,
			keyboard: true,
			wide: false
		};
		var effectiveOptions = $.extend({}, defaultOptions, options);
		
		var internal = {
			title: null,
			message: null,
			htmlMessage: null,
			buttons: [],
			el: null
		};
		
		$.extend(this, {
			title: function(title) {
				internal.title = title;
				if (documentr.isSomething(internal.el)) {
					$(internal.el).find('.modal-header h3').text(title);
				}
				return this;
			},
			
			message: function(message) {
				internal.message = message;
				internal.htmlMessage = null;
				if (documentr.isSomething(internal.el)) {
					$(internal.el).find('.modal-body').text(message);
				}
				return this;
			},
			
			htmlMessage: function(htmlMessage) {
				internal.htmlMessage = htmlMessage;
				internal.message = null;
				if (documentr.isSomething(internal.el)) {
					$(internal.el).find('.modal-body').html(htmlMessage);
				}
				return this;
			},
			
			close: function() {
				$(internal.el).modal('hide').remove();
				return this;
			},
			
			allButtonsDisabled: function(disabled) {
				$.each(internal.buttons, function(idx, button) {
					button.disabled(disabled);
				});
				return this;
			},
			
			button: function(button) {
				internal.buttons.push(button);
				if (documentr.isSomething(internal.el)) {
					$(internal.el).find('.modal-footer').append(button.createElement(this));
				}
				return this;
			},
			
			createElement: function() {
				var html =
					'<div class="modal" style="display: none;">' +
						'<div class="modal-header">' +
							'<button class="close">&#x00D7</button>' +
							'<h3></h3>' +
						'</div>' +
						'<div class="modal-body"></div>' +
						'<div class="modal-footer"></div>' +
					'</div>';
				internal.el = $.parseHTML(html);
				if (effectiveOptions.wide) {
					$(internal.el).addClass('modal-wide');
				}
				
				$(internal.el).find('.modal-header h3').text(internal.title);
				$(internal.el).find('.modal-header button').click(this.close);

				var bodyEl = $(internal.el).find('.modal-body');
				if (documentr.isSomething(internal.message)) {
					bodyEl.text(internal.message);
				} else if (documentr.isSomething(internal.htmlMessage)) {
					bodyEl.html(internal.htmlMessage);
				}

				var footerEl = $(internal.el).find('.modal-footer');
				var dlg = this;
				$.each(internal.buttons, function(idx, button) {
					footerEl.append(button.createElement(dlg));
				});
				
				return internal.el;
			},
			
			show: function() {
				this.createElement();
				var el = $(internal.el);
				el.modal({
					backdrop: effectiveOptions.backdrop,
					keyboard: effectiveOptions.keyboard
				});
				centerEl(el);
				return this;
			}
		});
	};
	
	return {
		DialogButton: DialogButton,
		Dialog: Dialog
	};
});
