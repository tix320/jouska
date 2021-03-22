package com.github.tix320.jouska.server.app;

import java.util.Set;

import com.github.tix320.deft.api.SystemProperties;
import com.github.tix320.jouska.core.application.game.ReadOnlyGameBoard;
import com.github.tix320.jouska.core.application.game.SimpleGame;
import com.github.tix320.jouska.core.application.game.TimedGame;
import com.github.tix320.jouska.core.application.tournament.ClassicGroup;
import com.github.tix320.jouska.core.application.tournament.ClassicGroup.GroupPlayer;
import com.github.tix320.jouska.core.application.tournament.ClassicPlayOff;
import com.github.tix320.jouska.core.application.tournament.ClassicTournament;
import com.github.tix320.jouska.core.application.tournament.PlayOffGame;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.model.Role;
import com.github.tix320.jouska.core.util.ClassUtils;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.MapperOptions;

public class DatastoreProvider {

	private final Datastore datastore;

	public DatastoreProvider() {
		datastore = createDatasource();
	}

	public Datastore getInstance() {
		return datastore;
	}

	private static Datastore createDatasource() {
		String dbHost = SystemProperties.getFromEnvOrElseJava("jouska.db.host", "localhost");
		int dbPort = Integer.parseInt(SystemProperties.getFromEnvOrElseJava("jouska.db.port", "27017"));
		String dbName = SystemProperties.getFromEnvOrElseJava("jouska.db.name", "jouska");
		String dbUsername = SystemProperties.getFromEnvOrElseJava("jouska.db.username", "admin");
		String dbPassword = SystemProperties.getFromEnvOrElseJava("jouska.db.password", "foo");

		System.out.println("Connecting to db...");
		System.out.println("Host: " + dbHost);
		System.out.println("Port: " + dbPort);
		System.out.println("Db Name: " + dbName);

		ConnectionString connectionString = new ConnectionString("mongodb://%s:%s/".formatted(dbHost, dbPort));
		MongoCredential credential = MongoCredential.createScramSha1Credential(dbUsername, "admin",
				dbPassword.toCharArray());

		MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).
				credential(credential).build();

		final MongoClient mongoClient = MongoClients.create(mongoClientSettings);

		final MapperOptions mapperOptions = MapperOptions.builder()
				.mapSubPackages(true)
				.storeEmpties(true)
				.enablePolymorphicQueries(true)
				.build();

		Datastore datastore = Morphia.createDatastore(mongoClient, dbName, mapperOptions);

		datastore.getMapper().mapPackage("com.github.tix320.jouska.server.infrastructure.entity");

		mapExternalClasses(datastore.getMapper(),
				ClassUtils.getPackageClasses("com.github.tix320.jouska.core.application.game"));
		mapExternalClasses(datastore.getMapper(),
				ClassUtils.getPackageClasses("com.github.tix320.jouska.core.application.game.creation"));
		mapExternalClasses(datastore.getMapper(),
				ClassUtils.getPackageClasses("com.github.tix320.jouska.core.application.tournament"));

		mapExternalClasses(datastore.getMapper(), Player.class, Role.class);

		datastore.ensureIndexes();

		return datastore;
	}

	private static final Set<Class<?>> exclusions = Set.of(ReadOnlyGameBoard.class, SimpleGame.class, TimedGame.class,
			ClassicGroup.class, GroupPlayer.class, ClassicPlayOff.class, ClassicTournament.class, PlayOffGame.class);

	private static void mapExternalClasses(Mapper mapper, Class<?>... classes) {
		for (Class<?> clazz : classes) {
			if (!clazz.isEnum() && !Throwable.class.isAssignableFrom(clazz) && !exclusions.contains(clazz)) {
				mapper.mapExternal(null, clazz);
			}
		}
	}
}
