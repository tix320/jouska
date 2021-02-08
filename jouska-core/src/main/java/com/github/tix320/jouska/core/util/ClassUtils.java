package com.github.tix320.jouska.core.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

/**
 * @author : Tigran Sargsyan
 * @since : 07.02.2021
 **/
public class ClassUtils {

	public static Class<?>[] getPackageClasses(String packageName) {
		try (ScanResult scanResult = new ClassGraph().acceptPackages(packageName).enableClassInfo().scan()) {
			return scanResult.getAllClasses().getNames().stream().map(s -> {
				try {
					return Class.forName(s);
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e);
				}
			}).toArray(Class[]::new);
		}
	}
}
