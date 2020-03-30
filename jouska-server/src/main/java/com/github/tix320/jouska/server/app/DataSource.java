package com.github.tix320.jouska.server.app;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class DataSource {

	public static Datastore INSTANCE;

	public static void init() {
		String dbHost = getProperty("jouskaDbHost", "localhost");
		int dbPort = Integer.parseInt(getProperty("jouskaDbPort", "27017"));
		String dbName = getProperty("jouskaDbName", "jouska");
		String dbUsername = getProperty("jouskaDbUsername", "admin");
		String dbPassword = getProperty("jouskaDbPassword", "");

		System.out.println("Connecting to db...");
		System.out.println("Host: " + dbHost);
		System.out.println("Port: " + dbPort);
		System.out.println("Db Name: " + dbName);

		final Morphia morphia = new Morphia();

		// tell Morphia where to find your classes
		// can be called multiple times with different packages or classes
		morphia.mapPackage("com.github.tix320.jouska.server.entity");

		ServerAddress serverAddress = new ServerAddress(dbHost, dbPort);
		MongoCredential credential = MongoCredential.createScramSha1Credential(dbUsername, "admin",
				dbPassword.toCharArray());
		final Datastore datastore = morphia.createDatastore(
				new MongoClient(serverAddress, credential, MongoClientOptions.builder().build()), dbName);
		datastore.ensureIndexes();
		INSTANCE = datastore;
	}

	private static String getProperty(String key, String defaultValue) {
		String value = System.getProperty(key);
		if (value == null) {
			value = System.getenv(key);

			if (value == null) {
				value = defaultValue;
			}
		}

		return value;
	}
}
