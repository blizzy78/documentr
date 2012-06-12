/*
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
*/

$.fn.extend({
	showModal: function(options) {
		this.modal(options);
		this.position({
			my: 'center center',
			at: 'center center',
			of: window
		});
	},
	
	hideModal: function() {
		this.modal('hide');
	},
	
	setPreventClick: function(preventClick) {
		var preventClickHandler = this.data('preventClickHandler');
		if ((preventClickHandler == null) && preventClick) {
			preventClickHandler = function(event) {
				event.preventDefault();
			};
			this.data('preventClickHandler', preventClickHandler);
			this.bind('click', preventClickHandler);
		} else if ((preventClickHandler != null) && !preventClick) {
			this.unbind('click', preventClickHandler);
			this.data('preventClickHandler', null);
		}
		return this;
	},
	
	setButtonDisabled: function(disabled) {
		if (disabled) {
			this.addClass('disabled');
		} else {
			this.removeClass('disabled');
		}
		this.setPreventClick(disabled);
		return this;
	}
});
