package com.github.tix320.jouska.server.infrastructure.dao.query.filter;

import java.util.List;

import dev.morphia.query.Criteria;
import dev.morphia.query.CriteriaContainerImpl;
import dev.morphia.query.Query;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public class NotInFilter implements Filter {

	private final String field;

	private final List<?> values;

	NotInFilter(String field, List<?> values) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException("Empty");
		}
		this.field = field;
		this.values = values;
	}

	@Override
	public Criteria applyTo(Query<?> query) {
		return query.criteria(field).notIn(values);
	}

	@Override
	public Filter not() {
		return new InFilter(field, values);
	}
}
