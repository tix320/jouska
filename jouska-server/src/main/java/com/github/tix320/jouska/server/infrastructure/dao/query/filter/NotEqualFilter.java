package com.github.tix320.jouska.server.infrastructure.dao.query.filter;

import dev.morphia.query.Criteria;
import dev.morphia.query.Query;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public class NotEqualFilter implements Filter {

	private final String field;

	private final Object value;

	NotEqualFilter(String field, Object value) {
		this.field = field;
		this.value = value;
	}

	@Override
	public Criteria applyTo(Query<?> query) {
		return query.criteria(field).notEqual(value);
	}

	@Override
	public Filter not() {
		return new EqualFilter(field, value);
	}
}
