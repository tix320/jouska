package com.github.tix320.jouska.server.app;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class DataSource {

	public static Datastore INSTANCE;

	public static void init(String dbHost, int dbPort) {
		final Morphia morphia = new Morphia();

		// tell Morphia where to find your classes
		// can be called multiple times with different packages or classes
		morphia.mapPackage("com.github.tix320.jouska.server.entity");

		final Datastore datastore = morphia.createDatastore(new MongoClient(new ServerAddress(dbHost, dbPort)),
				"jouska");
		datastore.ensureIndexes();
		INSTANCE = datastore;
	}
}
