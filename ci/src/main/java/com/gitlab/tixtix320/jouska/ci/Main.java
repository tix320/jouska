package com.gitlab.tixtix320.jouska.ci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

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

		List<Path> jars = Files.walk(libPath, 1).collect(Collectors.toList());
		jars.remove(0); // remove /lib

		StringBuilder runCommand = new StringBuilder();
		runCommand.append("java --module-path ");
		runCommand.append(mainJarFile.getFileName()).append(";");
		for (Path jar : jars) {
			runCommand.append(jar.getFileName()).append(";");
		}
		runCommand.append(" -m ").append(MAIN_MODULE).append("/").append(MAIN_CLASS);

		System.out.println(runCommand);
	}
}
