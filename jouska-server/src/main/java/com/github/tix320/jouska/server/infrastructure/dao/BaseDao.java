package com.github.tix320.jouska.server.infrastructure.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.github.tix320.jouska.server.app.DatastoreProvider;
import com.github.tix320.jouska.server.infrastructure.entity.Identifiable;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperator;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public abstract class BaseDao<T extends Identifiable> {

	private final DatastoreProvider datastoreProvider;

	protected BaseDao(DatastoreProvider datastoreProvider) {
		this.datastoreProvider = datastoreProvider;
	}

	public final Optional<T> findById(String id) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());
		query.filter(Filters.eq("_id", new ObjectId(id)));
		return Optional.ofNullable(query.first());
	}

	public final Optional<T> findById(String id, String... fieldsToFetch) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		FindOptions findOptions;
		if (fieldsToFetch.length == 0) {
			findOptions = new FindOptions().projection().include("_id");
		} else {
			findOptions = new FindOptions().projection().include(fieldsToFetch);
		}

		query.filter(Filters.eq("_id", new ObjectId(id)));

		final T item = query.first(findOptions);

		return Optional.ofNullable(item);
	}

	public final List<T> findAll() {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		return query.iterator().toList();
	}

	public final List<T> findAll(String... fieldsToFetch) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		FindOptions findOptions;
		if (fieldsToFetch.length == 0) {
			findOptions = new FindOptions().projection().include("_id");
		} else {
			findOptions = new FindOptions().projection().include(fieldsToFetch);
		}

		return query.iterator(findOptions).toList();
	}

	public final List<T> findAll(Filter filter) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		query.filter(filter);

		return query.iterator().toList();
	}

	public final List<T> findAll(Filter filter, String... fieldsToFetch) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		FindOptions findOptions;
		if (fieldsToFetch.length == 0) {
			findOptions = new FindOptions().projection().include("_id");
		} else {
			findOptions = new FindOptions().projection().include(fieldsToFetch);
		}

		query.filter(filter);

		return query.iterator(findOptions).toList();
	}


	public final Optional<T> find(Filter filter) {
		List<T> list = findAll(filter);
		if (list.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(list.get(0));
		}
	}

	public final String save(T entity) {
		datastoreProvider.getInstance().save(entity);
		return entity.getId();
	}

	public final void update(T entity) {
		Datastore instance = datastoreProvider.getInstance();
		instance.save(entity);
	}

	public final void update(T entity, Map<String, Function<T, ?>> fieldsToUpdate) {
		if (fieldsToUpdate.isEmpty()) {
			throw new IllegalArgumentException("Empty map");
		}

		Datastore instance = datastoreProvider.getInstance();

		final Query<T> query = instance.find(getEntityClass());

		query.filter(Filters.eq("_id", new ObjectId(entity.getId())));

		final Iterator<Entry<String, Function<T, ?>>> iterator = fieldsToUpdate.entrySet().iterator();

		final Entry<String, Function<T, ?>> firstUpdate = iterator.next();
		final UpdateOperator firstUpdateOperator = UpdateOperators.set(firstUpdate.getKey(),
				firstUpdate.getValue().apply(entity));


		UpdateOperator[] remainingOperators = new UpdateOperator[fieldsToUpdate.size() - 1];
		AtomicInteger index = new AtomicInteger(0);
		iterator.forEachRemaining(
				entry -> remainingOperators[index.getAndIncrement()] = UpdateOperators.set(entry.getKey(),
						entry.getValue().apply(entity)));

		query.update(firstUpdateOperator, remainingOperators).execute();
	}

	public final void deleteById(String id) {
		Datastore instance = datastoreProvider.getInstance();

		instance.find(getEntityClass()).filter(Filters.eq("_id", new ObjectId(id))).delete();
	}

	protected abstract Class<T> getEntityClass();
}
