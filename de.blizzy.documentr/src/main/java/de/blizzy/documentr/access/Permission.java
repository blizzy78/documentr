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
package de.blizzy.documentr.access;

/** Permissions that can be granted on objects. */
public enum Permission {
	/** Administrative permission. Can be granted on all objects. Implies all other permissions on those objects. */
	ADMIN,

	/** Viewing permission. Can be granted on all objects. */
	VIEW,

	/**
	 * <p>Edit project permission. Can be granted on:</p>
	 *
	 * <ul>
	 *   <li>Application - Allows to create, edit, and delete projects.</li>
	 *   <li>Project - Allows to edit project details.</li>
	 * </ul>
	 */
	EDIT_PROJECT,

	/**
	 * <p>Edit branch permission. Can be granted on:</p>
	 *
	 * <ul>
	 *   <li>Application, project - Allows to create, edit, and delete branches.</li>
	 *   <li>Branch - Allows to edit branch details.</li>
	 * </ul>
	 */
	EDIT_BRANCH,

	/**
	 * <p>Edit page permission. Can be granted on:</p>
	 *
	 * <ul>
	 *   <li>Application, project, branch - Allows to create, edit, and delete pages and attachments.</li>
	 * </ul>
	 */
	EDIT_PAGE;
}
