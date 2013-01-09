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
package de.blizzy.documentr.page;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;

public class PageMetadata implements Serializable {
	private static final long serialVersionUID = -3763833054250717367L;

	@Getter
	private String lastEditedBy;
	@Getter
	private Date lastEdited;
	@Getter
	private long size;
	@Getter
	private String commit;

	public PageMetadata(String lastEditedBy, Date lastEdited, long size, String commit) {
		this.lastEditedBy = lastEditedBy;
		this.lastEdited = lastEdited;
		this.size = size;
		this.commit = commit;
	}
}
