package com.github.tix320.jouska.ci.jre;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tigran Sargsyan on 31-Mar-20.
 */
public class BotJRE {

	private static final String[] REQUIRED_MODULE_NAMES = {
			"sonder", "kiwi", "jouska.core"};

	public static void main(String[] args)
			throws IOException, InterruptedException {
		String runnerOS = args[0];
		Path libPath = Path.of(args[1]);
		Path jdkPath = Path.of(args[2]);
		Path targetPath = Path.of(args[3]);

		char modulePathSeparator;
		switch (runnerOS.toLowerCase()) {
			case "unix":
				modulePathSeparator = ':';
				break;
			case "windows":
				modulePathSeparator = ';';
				break;
			default:
				throw new IllegalArgumentException("Invalid OS: " + runnerOS);
		}

		checkDirectoryExist(libPath);
		checkDirectoryExist(jdkPath);
		checkDirectoryExist(targetPath);

		List<Path> jars = Files.walk(libPath, 1).skip(1).collect(Collectors.toList());

		Iterator<Path> iterator = jars.iterator();
		while (iterator.hasNext()) {
			Path jar = iterator.next();
			if (jar.getFileName().toString().contains("byte-buddy")) {
				Files.delete(jar);
				iterator.remove();
			}
		}

		StringBuilder modulePath = new StringBuilder();
		for (Path jar : jars) {
			modulePath.append(libPath).append(File.separatorChar).append(jar.getFileName()).append(modulePathSeparator);
		}

		System.out.println("Generating Bot JRE...");

		String jlinkCommand = "jlink --module-path "
							  + jdkPath
							  + "/jmods"
							  + modulePathSeparator
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
			throw new IllegalArgumentException("Directory does not exists: " + path);
		}
	}
}
