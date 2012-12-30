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
package de.blizzy.documentr.web.macro;

import javax.validation.constraints.NotNull;

import lombok.Getter;

import org.hibernate.validator.constraints.NotBlank;

import de.blizzy.documentr.validation.annotation.ValidMacroName;

public class MacroForm {
	@NotNull(message="{macro.name.blank}")
	@NotBlank(message="{macro.name.blank}")
	@ValidMacroName
	@Getter
	private String name;
	@Getter
	private String code;

	public MacroForm(String name, String code) {
		this.name = name;
		this.code = code;
	}
}
