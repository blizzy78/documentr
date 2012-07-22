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
package org.pegdown;

import org.apache.commons.lang3.StringUtils;
import org.parboiled.Rule;
import org.parboiled.annotations.Cached;
import org.parboiled.common.ArrayBuilder;
import org.parboiled.support.StringBuilderVar;
import org.parboiled.support.StringVar;

import de.blizzy.documentr.web.markdown.MacroNode;

public class DocumentrParser extends Parser {
	private static final int PEGDOWN_OPTIONS = Extensions.ALL -
			Extensions.QUOTES - Extensions.SMARTS - Extensions.SMARTYPANTS;

	public DocumentrParser() {
		super(Integer.valueOf(PEGDOWN_OPTIONS));
	}

	@Override
	public Rule NonLinkInline() {
		return FirstOf(new ArrayBuilder<Rule>()
				.add(
						BodyMacro(), StandaloneMacro(),
						Str(), Endline(), UlOrStarLine(), Space(), StrongOrEmph(), Image(), Code(), InlineHtml(),
						Entity(), EscapedChar())
				.addNonNulls(ext(QUOTES) ? new Rule[] { SingleQuoted(), DoubleQuoted(), DoubleAngleQuoted() } : null)
				.addNonNulls(ext(SMARTS) ? new Rule[] { Smarts() } : null)
				.add(Symbol()).get());
	}

	@SuppressWarnings("boxing")
	public Rule StandaloneMacro() {
		StringVar macroName = new StringVar();
		StringVar params = new StringVar();
		return NodeSequence(
				"{{", //$NON-NLS-1$
				MacroNameAndParameters(macroName, params, true),
				push(new MacroNode(macroName.get(), params.get())),
				"/}}"); //$NON-NLS-1$
	}
	
	@SuppressWarnings("boxing")
	public Rule BodyMacro() {
		StringVar macroName = new StringVar();
		StringVar params = new StringVar();
		StringBuilderVar inner = new StringBuilderVar();
		return NodeSequence(
				"{{", //$NON-NLS-1$
				MacroNameAndParameters(macroName, params, false),
				"}}", //$NON-NLS-1$
				push(getContext().getCurrentIndex()),
				BodyMacroBody(inner, macroName),
				"{{/", //$NON-NLS-1$
				MacroName(macroName),
				"}}", //$NON-NLS-1$
				push(new MacroNode(macroName.get(), params.get(),
						withIndicesShifted(parseInternal(inner.appended("\n\n")), (Integer) pop()).getChildren()))); //$NON-NLS-1$
	}
	
	@SuppressWarnings("boxing")
	public Rule BodyMacroBody(StringBuilderVar inner, StringVar macroName) {
		return Sequence(
				ZeroOrMore(BlankLine()),
				ZeroOrMore(
						TestNot(Sequence("{{/", MacroName(macroName), "}}")), //$NON-NLS-1$ //$NON-NLS-2$
						ANY,
						inner.append(match())),
				ZeroOrMore(BlankLine()));
	}
	
	public Rule MacroNameAndParameters(StringVar macroName, StringVar params, boolean standalone) {
		return Sequence(
				MacroName(macroName),
				Optional(
						Sequence(
								Spacechar(),
								MacroParameters(params, standalone)
						)
				)
		);
	}

	@SuppressWarnings("boxing")
	public Rule MacroName(StringVar macroName) {
		return Sequence(
				OneOrMore(Alphanumeric()),
				macroName.isSet() && match().equals(macroName.get()) ||
					macroName.isNotSet() && macroName.set(match()));
	}
	
	@SuppressWarnings("boxing")
	public Rule MacroParameters(StringVar params, boolean standalone) {
		return Sequence(
				OneOrMore(
						TestNot(standalone ? "/}}" : "}}"), //$NON-NLS-1$ //$NON-NLS-2$
						ANY),
				params.isSet() && match().equals(params.get()) ||
					params.isNotSet() && params.set(match()));
	}

	@Override
	@Cached
	@SuppressWarnings("boxing")
	public Rule LinkSource() {
		StringBuilderVar url = new StringBuilderVar();
		return FirstOf(
				Sequence('(', LinkSource(), ')'),
				Sequence('<', LinkSource(), '>'),
				Sequence('#', AnchorName(url)),
				Sequence(TestNot(AnyOf("#")), Url(url)), //$NON-NLS-1$
				push(StringUtils.EMPTY)
		);
	}
	
	@SuppressWarnings("boxing")
	public Rule AnchorName(StringBuilderVar url) {
		return Sequence(AnchorNameChars(url), push("#" + url.getString())); //$NON-NLS-1$
	}

	@SuppressWarnings("boxing")
	public Rule Url(StringBuilderVar url) {
		return Sequence(UrlChars(url), push(url.getString()));
	}
	
	@SuppressWarnings("boxing")
	public Rule AnchorNameChars(StringBuilderVar url) {
		return OneOrMore(
				Sequence(NoneOf("()<>[]#"), url.append(matchedChar())) //$NON-NLS-1$
		);
	}

	@SuppressWarnings("boxing")
	public Rule UrlChars(StringBuilderVar url) {
		return OneOrMore(
				FirstOf(
						Sequence('\\', AnyOf("()"), url.append(matchedChar())), //$NON-NLS-1$
						Sequence(
								TestNot(AnyOf("()>")), //$NON-NLS-1$
								FirstOf(Nonspacechar(), Spacechar(), '|'),
								url.append(matchedChar()))
				)
		);
	}
}
