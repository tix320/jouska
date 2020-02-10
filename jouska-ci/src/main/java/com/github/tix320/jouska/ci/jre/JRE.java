package com.github.tix320.jouska.ci.jre;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
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

		Process jlinkProcess = Runtime.getRuntime().exec(jlinkCommand, null, targetPath.toFile());
		byte[] bytes = jlinkProcess.getInputStream().readAllBytes();
		String output = new String(bytes);
		System.out.println(output);


	}
}
