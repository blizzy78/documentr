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
package de.blizzy.documentr.markdown.macro.impl;


import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.blizzy.documentr.markdown.macro.IMacro;
import de.blizzy.documentr.markdown.macro.IMacroDescriptor;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;

@Component
public class TableOfContentsMacro implements IMacro {
	@Autowired
	private BeanFactory beanFactory;
	
	@Override
	public IMacroDescriptor getDescriptor() {
		return MessageSourceMacroDescriptor.create("toc", beanFactory) //$NON-NLS-1$
			.insertText("{{toc/}}"); //$NON-NLS-1$
	}

	@Override
	public IMacroRunnable createRunnable() {
		return new TableOfContentsMacroRunnable();
	}
}
