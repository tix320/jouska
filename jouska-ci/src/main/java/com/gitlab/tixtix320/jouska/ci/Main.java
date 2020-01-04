package com.gitlab.tixtix320.jouska.ci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

	private static final String[] REQUIRED_MODULE_NAMES = {
			"javafx.base",
			"javafx.graphics",
			"javafx.controls",
			"javafx.fxml",
			"sonder",
			"kiwi",
			"jouska.core"
	};
	private static final String MAIN_JAR_FILE_NAME = "jouska-app.jar";
	private static final String MAIN_MODULE = "jouska.client";
	private static final String MAIN_CLASS = "com.gitlab.tixtix320.jouska.client.app.Main";

	public static void main(String[] args)
			throws InterruptedException, IOException {
		Path targetPath = Path.of(args[0]);

		Path mainJarFile = Files.find(targetPath, 1,
				(path, basicFileAttributes) -> path.getFileName().toString().equals(MAIN_JAR_FILE_NAME))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						String.format("Main jar file not found %s", MAIN_JAR_FILE_NAME)));


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
			if (javafxEmptyJars.contains(jar.getFileName().toString())) {
				Files.delete(jar);
				iterator.remove();
			}
		}

		StringBuilder modulePath = new StringBuilder();
		for (Path jar : jars) {
			modulePath.append("lib/").append(jar.getFileName()).append(";");
		}

		String jlinkCommand = "jlink --module-path " + modulePath + " --add-modules " + String.join(",",
				REQUIRED_MODULE_NAMES) + " --output jre --compress=2";

		Runtime.getRuntime().exec(jlinkCommand, null, targetPath.toFile()).waitFor();

		String runCommand = "jre\\bin\\java --module-path "
							+ mainJarFile.getFileName().toString()
							+ " -m "
							+ MAIN_MODULE
							+ "/"
							+ MAIN_CLASS;
		System.out.println(runCommand);
	}
}
