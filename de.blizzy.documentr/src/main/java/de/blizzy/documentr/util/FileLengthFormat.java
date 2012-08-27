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
package de.blizzy.documentr.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

import org.springframework.context.MessageSource;

/**
 * Formats a file's length into human-readable text format.
 * 
 * @see #format(long)
 */
public class FileLengthFormat extends Format {
	private MessageSource messageSource;
	private Locale locale;

	/**
	 * Constructs a new file length formatter.
	 * 
	 * @param messageSource a source of message texts
	 * @param locale the locale to use
	 */
	public FileLengthFormat(MessageSource messageSource, Locale locale) {
		this.messageSource = messageSource;
		this.locale = locale;
	}
	
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (!(obj instanceof Long)) {
			throw new IllegalArgumentException("object must be Long"); //$NON-NLS-1$
		}

		long length = ((Number) obj).longValue();
		if (length < 1024L) {
			toAppendTo.append(messageSource.getMessage(
					"sizeX.bytes", new Object[] { formatNumber(length) }, locale)); //$NON-NLS-1$
		} else if (length < 1024000L) {
			toAppendTo.append(messageSource.getMessage(
					"sizeX.kb", new Object[] { formatNumber(length / 1024d) }, locale)); //$NON-NLS-1$
		} else if (length < 1048576000L) {
			toAppendTo.append(messageSource.getMessage(
					"sizeX.mb", new Object[] { formatNumber(length / 1024d / 1024d) }, locale)); //$NON-NLS-1$
		} else {
			toAppendTo.append(messageSource.getMessage(
					"sizeX.gb", new Object[] { formatNumber(length / 1024d / 1024d / 1024d) }, locale)); //$NON-NLS-1$
		}

		return toAppendTo;
	}

	/** Formats the specific length into human-readable format. */
	public String format(long length) {
		return format(Long.valueOf(length), new StringBuffer(), null).toString();
	}
	
	private String formatNumber(double d) {
		NumberFormat format = NumberFormat.getNumberInstance(locale);
		int fractionDigits;
		if (d >= 100d) {
			fractionDigits = 0;
		} else if (d >= 10d) {
			fractionDigits = 1;
		} else {
			fractionDigits = 2;
		}
		format.setMaximumFractionDigits(fractionDigits);
		return format.format(d);
	}
	
	private String formatNumber(long l) {
		return NumberFormat.getNumberInstance().format(l);
	}

	/** Returns <code>null</code> since this class does not support parsing. */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		return null;
	}
}
