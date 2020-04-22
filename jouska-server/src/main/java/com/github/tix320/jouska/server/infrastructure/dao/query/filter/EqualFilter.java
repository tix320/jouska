package com.github.tix320.jouska.server.infrastructure.dao.query.filter;

import dev.morphia.query.Criteria;
import dev.morphia.query.Query;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public final class EqualFilter implements Filter {

	private final String field;

	private final Object value;

	EqualFilter(String field, Object value) {
		this.field = field;
		this.value = value;
	}

	@Override
	public Criteria applyTo(Query<?> query) {
		return query.criteria(field).equal(value);
	}

	@Override
	public Filter not() {
		return new NotEqualFilter(field, value);
	}
}
