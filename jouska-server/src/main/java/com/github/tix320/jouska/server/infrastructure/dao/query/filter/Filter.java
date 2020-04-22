package com.github.tix320.jouska.server.infrastructure.dao.query.filter;

import java.util.List;

import dev.morphia.query.Criteria;
import dev.morphia.query.Query;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public interface Filter {

	Criteria applyTo(Query<?> query);

	Filter not();

	// Factory methods

	static Filter equal(String field, Object value) {
		return new EqualFilter(field, value);
	}

	static Filter notEqual(String field, Object value) {
		return new NotEqualFilter(field, value);
	}

	static Filter in(String field, List<?> values) {
		return new InFilter(field, values);
	}

	static Filter notIn(String field, List<?> values) {
		return new NotInFilter(field, values);
	}

	static Filter and(Filter filter1, Filter filter2) {
		return new AndFilter(filter1, filter2);
	}
}
