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
		String dbHost = System.getProperty("dbHost", "localhost");
		int dbPort = Integer.parseInt(System.getProperty("dbPort", "27017"));
		String dbName = System.getProperty("dbName", "jouska");
		String dbUsername = System.getProperty("dbUsername", "admin");
		String dbPassword = System.getProperty("dbPassword", "");

		final Morphia morphia = new Morphia();

		// tell Morphia where to find your classes
		// can be called multiple times with different packages or classes
		morphia.mapPackage("com.github.tix320.jouska.server.entity");

		ServerAddress serverAddress = new ServerAddress(dbHost, dbPort);
		MongoCredential credential = MongoCredential.createScramSha1Credential(dbUsername, "admin", dbPassword.toCharArray());
		final Datastore datastore = morphia.createDatastore(
				new MongoClient(serverAddress, credential, MongoClientOptions.builder().build()), dbName);
		datastore.ensureIndexes();
		INSTANCE = datastore;
	}
}
