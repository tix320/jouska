package com.github.tix320.jouska.server.infrastructure.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.github.tix320.jouska.server.app.DatastoreProvider;
import com.github.tix320.jouska.server.infrastructure.dao.query.filter.Filter;
import com.github.tix320.jouska.server.infrastructure.entity.Identifiable;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
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
		query.field("_id").equal(new ObjectId(id));
		return Optional.ofNullable(query.first());
	}

	public final Optional<T> findById(String id, List<String> fieldsToFetch) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		if (fieldsToFetch.isEmpty()) {
			query.project("_id", true);
		}
		else {
			for (String field : fieldsToFetch) {
				query.project(field, true);
			}
		}

		query.field("_id").equal(new ObjectId(id));

		return Optional.ofNullable(query.first());
	}

	public final List<T> findAll() {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		return query.find().toList();
	}

	public final List<T> findAll(List<String> fieldsToFetch) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		if (fieldsToFetch.isEmpty()) {
			query.project("_id", true);
		}
		else {
			for (String field : fieldsToFetch) {
				query.project(field, true);
			}
		}

		return query.find().toList();
	}

	public final List<T> findAll(Filter filter) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		filter.applyTo(query);

		return query.find().toList();
	}

	public final List<T> findAll(List<String> fieldsToFetch, Filter filter) {
		Query<T> query = datastoreProvider.getInstance().find(getEntityClass());

		if (fieldsToFetch.isEmpty()) {
			query.project("_id", true);
		}
		else {
			for (String field : fieldsToFetch) {
				query.project(field, true);
			}
		}

		filter.applyTo(query);

		return query.find().toList();
	}


	public final Optional<T> find(Filter filter) {
		List<T> list = findAll(filter);
		if (list.isEmpty()) {
			return Optional.empty();
		}
		else {
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
		Datastore instance = datastoreProvider.getInstance();

		Query<T> findQuery = instance.createQuery(getEntityClass()).field("_id").equal(new ObjectId(entity.getId()));
		UpdateOperations<T> updateOperations = instance.createUpdateOperations(getEntityClass());

		fieldsToUpdate.forEach(
				(fieldName, fieldRetriever) -> updateOperations.set(fieldName, fieldRetriever.apply(entity)));

		instance.update(findQuery, updateOperations);
	}

	public final void deleteById(String id) {
		Datastore instance = datastoreProvider.getInstance();

		instance.delete(instance.createQuery(getEntityClass()).field("_id").equal(new ObjectId(id)));
	}

	protected abstract Class<T> getEntityClass();
}
