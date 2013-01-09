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
package de.blizzy.documentr.system;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.util.Assert;

@EqualsAndHashCode
class Version implements Comparable<Version> {
	@Getter(AccessLevel.PACKAGE)
	private int major;
	@Getter(AccessLevel.PACKAGE)
	private int minor;
	@Getter(AccessLevel.PACKAGE)
	private int maintenance;

	private Version(int major, int minor, int maintenance) {
		this.major = major;
		this.minor = minor;
		this.maintenance = maintenance;
	}

	static Version fromString(String s) {
		Assert.isTrue(s.matches("^[0-9]+\\.[0-9]+\\.[0-9]+(-.+)?$")); //$NON-NLS-1$

		s = s.replaceFirst("-.*$", StringUtils.EMPTY); //$NON-NLS-1$
		String[] parts = s.split("\\."); //$NON-NLS-1$
		return new Version(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
	}

	@Override
	public int compareTo(Version v) {
		return new CompareToBuilder()
			.append(major, v.major)
			.append(minor, v.minor)
			.append(maintenance, v.maintenance)
			.toComparison();
	}

	@Override
	public String toString() {
		return String.valueOf(major) + "." + String.valueOf(minor) + "." + String.valueOf(maintenance); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
