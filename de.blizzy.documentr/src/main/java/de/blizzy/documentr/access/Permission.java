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
package de.blizzy.documentr.access;

public enum Permission {
	// ===== VALID TARGET TYPES: all =====

	// can do anything (implies all other permissions on same target)
	ADMIN,
	
	// simple viewing
	VIEW,
	

	// ===== VALID TARGET TYPES: application, project =====
	
	// application: create/edit/delete projects
	// project: edit details
	EDIT_PROJECT,

	
	// ===== VALID TARGET TYPES: application, project, branch =====
	
	// application/project: create/edit/delete branches
	// branch: edit details
	EDIT_BRANCH,
	
	// application/project/branch: create/edit/delete pages
	EDIT_PAGE;
}
