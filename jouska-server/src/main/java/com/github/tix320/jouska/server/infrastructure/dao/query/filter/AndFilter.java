package com.github.tix320.jouska.server.infrastructure.dao.query.filter;

import dev.morphia.query.Criteria;
import dev.morphia.query.Query;

/**
 * @author Tigran Sargsyan on 18-Apr-20.
 */
public class AndFilter implements Filter {

	private final Filter filter1;

	private final Filter filter2;

	AndFilter(Filter filter1, Filter filter2) {
		this.filter1 = filter1;
		this.filter2 = filter2;
	}

	@Override
	public Criteria applyTo(Query<?> query) {
		return query.and(filter1.applyTo(query), filter2.applyTo(query));
	}

	@Override
	public Filter not() {
		throw new UnsupportedOperationException(); //TODO
	}
}
