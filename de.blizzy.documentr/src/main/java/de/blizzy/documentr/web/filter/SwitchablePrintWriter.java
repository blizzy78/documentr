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
package de.blizzy.documentr.web.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

import org.apache.commons.io.output.NullWriter;

abstract class SwitchablePrintWriter extends PrintWriter {
	SwitchablePrintWriter() {
		super(NullWriter.NULL_WRITER);
	}

	abstract Writer getWriter() throws IOException;

	private void setWriter() {
		try {
			out = getWriter();
		} catch (IOException e) {
			setError();
		}
	}

	@Override
	public void flush() {
		setWriter();
		super.flush();
	}

	@Override
	public void close() {
		setWriter();
		super.close();
	}

	@Override
	public boolean checkError() {
		setWriter();
		return super.checkError();
	}

	@Override
	public void write(int c) {
		setWriter();
		super.write(c);
	}

	@Override
	public void write(char[] buf, int off, int len) {
		setWriter();
		super.write(buf, off, len);
	}

	@Override
	public void write(char[] buf) {
		setWriter();
		super.write(buf);
	}

	@Override
	public void write(String s, int off, int len) {
		setWriter();
		super.write(s, off, len);
	}

	@Override
	public void write(String s) {
		setWriter();
		super.write(s);
	}

	@Override
	public void print(boolean b) {
		setWriter();
		super.print(b);
	}

	@Override
	public void print(char c) {
		setWriter();
		super.print(c);
	}

	@Override
	public void print(int i) {
		setWriter();
		super.print(i);
	}

	@Override
	public void print(long l) {
		setWriter();
		super.print(l);
	}

	@Override
	public void print(float f) {
		setWriter();
		super.print(f);
	}

	@Override
	public void print(double d) {
		setWriter();
		super.print(d);
	}

	@Override
	public void print(char[] s) {
		setWriter();
		super.print(s);
	}

	@Override
	public void print(String s) {
		setWriter();
		super.print(s);
	}

	@Override
	public void print(Object obj) {
		setWriter();
		super.print(obj);
	}

	@Override
	public void println() {
		setWriter();
		super.println();
	}

	@Override
	public void println(boolean x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(char x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(int x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(long x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(float x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(double x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(char[] x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(String x) {
		setWriter();
		super.println(x);
	}

	@Override
	public void println(Object x) {
		setWriter();
		super.println(x);
	}

	@Override
	public PrintWriter printf(String format, Object... args) {
		setWriter();
		return super.printf(format, args);
	}

	@Override
	public PrintWriter printf(Locale l, String format, Object... args) {
		setWriter();
		return super.printf(l, format, args);
	}

	@Override
	public PrintWriter format(String format, Object... args) {
		setWriter();
		return super.format(format, args);
	}

	@Override
	public PrintWriter format(Locale l, String format, Object... args) {
		setWriter();
		return super.format(l, format, args);
	}

	@Override
	public PrintWriter append(CharSequence csq) {
		setWriter();
		return super.append(csq);
	}

	@Override
	public PrintWriter append(CharSequence csq, int start, int end) {
		setWriter();
		return super.append(csq, start, end);
	}

	@Override
	public PrintWriter append(char c) {
		setWriter();
		return super.append(c);
	}
}
