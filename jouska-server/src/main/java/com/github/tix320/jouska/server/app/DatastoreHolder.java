package com.github.tix320.jouska.server.app;

import com.github.tix320.jouska.server.infrastructure.entity.PlayerEntity;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.MapperOptions;

public class DatastoreHolder {

	private final Datastore datastore;

	public DatastoreHolder() {
		datastore = createDatasource();
	}
	public Datastore getInstance() {
		return datastore;
	}

	private static Datastore createDatasource() {
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
		morphia.mapPackage("com.github.tix320.jouska.server.infrastructure.entity");

		morphia.map(PlayerEntity.class);

		configureMapper(morphia.getMapper());

		ServerAddress serverAddress = new ServerAddress(dbHost, dbPort);
		MongoCredential credential = MongoCredential.createScramSha1Credential(dbUsername, "admin",
				dbPassword.toCharArray());
		final Datastore datastore = morphia.createDatastore(
				new MongoClient(serverAddress, credential, MongoClientOptions.builder().build()), dbName);
		datastore.ensureIndexes();

		return datastore;
	}

	private static String getProperty(String key, String defaultValue) {
		String value = System.getenv(key);
		if (value == null) {
			value = System.getProperty(key, defaultValue);
		}

		return value;
	}

	private static void configureMapper(Mapper mapper) {
		MapperOptions mapperOptions = mapper.getOptions();
		mapperOptions.setStoreEmpties(true);
	}
}
