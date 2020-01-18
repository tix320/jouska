open module jouska.core {
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;

	exports com.github.tix320.jouska.core.config;
	exports com.github.tix320.jouska.core.model;
	exports com.github.tix320.jouska.core.dto;
}
