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
package de.blizzy.documentr.web.markdown.macro.impl;

import org.springframework.web.util.UriComponentsBuilder;

import de.blizzy.documentr.web.markdown.macro.AbstractMacro;
import de.blizzy.documentr.web.markdown.macro.MacroDescriptor;

public class YoutubeMacro extends AbstractMacro {
	public static final MacroDescriptor DESCRIPTOR = new MacroDescriptor("youtube", //$NON-NLS-1$
			"macro.youtube.title", "macro.youtube.description", YoutubeMacro.class, //$NON-NLS-1$ //$NON-NLS-2$
			"{{youtube [VIDEO]/}}"); //$NON-NLS-1$

	@Override
	public String getHtml() {
		String videoId = getParameters().trim();
		if (videoId.startsWith("http://") || videoId.startsWith("https://")) { //$NON-NLS-1$ //$NON-NLS-2$
			videoId = UriComponentsBuilder.fromHttpUrl(videoId).build().getQueryParams().get("v").get(0); //$NON-NLS-1$
		}
		
		return "<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/" + videoId + //$NON-NLS-1$
				"?rel=0\" frameborder=\"0\" allowfullscreen></iframe>"; //$NON-NLS-1$
	}
}
