package com.github.tix320.jouska.ci.jre;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JRE {

	private static final String[] REQUIRED_MODULE_NAMES = {
			"javafx.base",
			"javafx.graphics",
			"javafx.controls",
			"javafx.fxml",
			"sonder",
			"kiwi",
			"jouska.core"
	};

	public static void main(String[] args)
			throws InterruptedException, IOException {
		Path targetPath = Path.of(args[0]);
		String jdkPath = args[1];
		String javaFxJmodsPath = args[2];

		Path libPath = Path.of(targetPath.toString() + "/lib");

		if (!Files.exists(libPath) || !Files.isDirectory(libPath)) {
			throw new IllegalStateException("Lib directory does not exists");
		}

		List<Path> jars = Files.walk(libPath, 1).skip(1).collect(Collectors.toList());

		Set<String> javafxEmptyJars = Set.of("javafx-base-13.jar", "javafx-controls-13.jar", "javafx-fxml-13.jar",
				"javafx-graphics-13.jar");
		Iterator<Path> iterator = jars.iterator();
		while (iterator.hasNext()) {
			Path jar = iterator.next();
			if (jar.getFileName().toString().contains("javafx")) {
				Files.delete(jar);
				iterator.remove();
			}
		}

		StringBuilder modulePath = new StringBuilder();
		for (Path jar : jars) {
			modulePath.append("lib/").append(jar.getFileName()).append(";");
		}

		String jlinkCommand = "jlink --module-path "
							  + jdkPath
							  + "/jmods;"
							  + javaFxJmodsPath
							  + ";"
							  + modulePath
							  + " --add-modules "
							  + String.join(",", REQUIRED_MODULE_NAMES)
							  + " --output jre --no-header-files --no-man-pages --strip-debug --compress=2";

		int i = Runtime.getRuntime().exec(jlinkCommand, null, targetPath.toFile()).waitFor();
		if (i != 0) {
			throw new RuntimeException("Jlink failed");
		}
	}
}
