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
			"javafx.base", "javafx.graphics", "javafx.controls", "javafx.fxml", "sonder", "kiwi", "jouska.core",
			"net.bytebuddy",};

	public static void main(String[] args)
			throws IOException, InterruptedException {
		Path libPath = Path.of(args[0]);
		Path jdkPath = Path.of(args[1]);
		Path javaFxJmodsPath = Path.of(args[2]);
		Path targetPath = Path.of(args[3]);

		checkDirectoryExist(libPath);
		checkDirectoryExist(jdkPath);
		checkDirectoryExist(javaFxJmodsPath);
		checkDirectoryExist(targetPath);

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
			modulePath.append(libPath).append(File.separatorChar).append(jar.getFileName()).append(";");
		}

		System.out.println("Generating JRE...");

		String jlinkCommand = "jlink --module-path "
							  + jdkPath
							  + "/jmods;"
							  + javaFxJmodsPath
							  + ";"
							  + modulePath
							  + " --add-modules "
							  + String.join(",", REQUIRED_MODULE_NAMES)
							  + " --output "
							  + targetPath
							  + "/jre --no-header-files --no-man-pages --strip-debug --compress=2";

		System.out.println(jlinkCommand);

		Process jlinkProcess = Runtime.getRuntime().exec(jlinkCommand, null);
		byte[] bytes = jlinkProcess.getInputStream().readAllBytes();
		String output = new String(bytes);
		System.out.println(output);

		int exitCode = jlinkProcess.waitFor();

		if (exitCode == 0) {
			System.out.println("Generated successfully");
		}

		System.exit(exitCode);
	}

	private static void checkDirectoryExist(Path path) {
		if (!Files.exists(path) || !Files.isDirectory(path)) {
			throw new IllegalStateException("Directory does not exists: " + path);
		}
	}

}
