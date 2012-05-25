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
package de.blizzy.documentr.pagestore;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Page {
	private String parentPagePath;
	private String title;
	private String contentType;
	private PageData data;

	Page(String parentPagePath, String title, String contentType, PageData data) {
		this.parentPagePath = parentPagePath;
		this.title = title;
		this.contentType = contentType;
		this.data = data;
	}

	public static Page fromText(String parentPagePath, String title, String text) {
		PageTextData pageData = new PageTextData(text);
		return new Page(parentPagePath, title, pageData.getContentType(), pageData);
	}
	
	public static Page fromData(String parentPagePath, byte[] data, String contentType) {
		PageData pageData = new PageData(data, contentType);
		return new Page(parentPagePath, null, contentType, pageData);
	}
	
	public static Page fromMeta(String parentPagePath, String title, String contentType) {
		return new Page(parentPagePath, title, contentType, null);
	}

	public String getParentPagePath() {
		return parentPagePath;
	}
	
	public String getTitle() {
		return title;
	}

	public String getContentType() {
		return contentType;
	}
	
	public PageData getData() {
		return data;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if ((o != null) && o.getClass().equals(getClass())) {
			Page other = (Page) o;
			return new EqualsBuilder()
				.append(parentPagePath, other.parentPagePath)
				.append(title, other.title)
				.append(data, other.data)
				.append(contentType, other.contentType)
				.isEquals();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(parentPagePath)
			.append(title)
			.append(data)
			.append(contentType)
			.toHashCode();
	}
}
