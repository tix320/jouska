package com.github.tix320.jouska.core;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : Tigran Sargsyan
 * @since : 07.02.2021
 **/
public class Version implements Comparable<Version> {

	public static final Version CURRENT = resolveCurrentVersion();

	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{1,3})");

	private static final Comparator<Version> COMPARATOR = Comparator.comparingInt(Version::getMajor)
			.thenComparingInt(Version::getMinor)
			.thenComparingInt(Version::getPatch);

	private final int major;

	private final int minor;

	private final int patch;

	private Version() {
		this(-1, -1, -1);
	}

	public Version(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}

	public static Version fromString(String version) {
		Matcher matcher = VERSION_PATTERN.matcher(version);
		if (matcher.matches()) {
			int major = Integer.parseInt(matcher.group(1));
			int minor = Integer.parseInt(matcher.group(2));
			int patch = Integer.parseInt(matcher.group(3));

			return new Version(major, minor, patch);
		} else {
			throw new IllegalArgumentException("Invalid version string: " + version);
		}
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Version version = (Version) o;
		return major == version.major && minor == version.minor && patch == version.patch;
	}

	@Override
	public int hashCode() {
		return Objects.hash(major, minor, patch);
	}

	@Override
	public String toString() {
		return major + "." + minor + "." + patch;
	}

	@Override
	public int compareTo(Version version) {
		return COMPARATOR.compare(this, version);
	}

	private static Version resolveCurrentVersion() {
		String versionString = System.getProperty("jouska.version");

		Version version;
		if (versionString == null) {
			return new Version(0, 0, 0);
		} else {
			version = fromString(versionString);
		}

		return version;
	}
}
