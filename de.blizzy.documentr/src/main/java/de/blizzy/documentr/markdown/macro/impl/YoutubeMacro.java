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
package de.blizzy.documentr.markdown.macro.impl;

import org.springframework.web.util.UriComponentsBuilder;

import de.blizzy.documentr.markdown.macro.IMacroContext;
import de.blizzy.documentr.markdown.macro.IMacroRunnable;
import de.blizzy.documentr.markdown.macro.Macro;

@Macro(name="youtube", insertText="{{youtube [VIDEO]/}}")
public class YoutubeMacro implements IMacroRunnable {
	@Override
	public String getHtml(IMacroContext macroContext) {
		String videoId = macroContext.getParameters().trim();
		if (videoId.startsWith("http://") || videoId.startsWith("https://")) { //$NON-NLS-1$ //$NON-NLS-2$
			videoId = UriComponentsBuilder.fromHttpUrl(videoId).build().getQueryParams().get("v").get(0); //$NON-NLS-1$
		}

		return "<iframe width=\"560\" height=\"315\" src=\"http://www.youtube.com/embed/" + videoId + //$NON-NLS-1$
				"?rel=0\" frameborder=\"0\" allowfullscreen></iframe>"; //$NON-NLS-1$
	}

	@Override
	public String cleanupHtml(String html) {
		return null;
	}
}
