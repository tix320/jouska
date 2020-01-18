open module jouska.core {
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;

	exports com.gitlab.tixtix320.jouska.core.config;
	exports com.gitlab.tixtix320.jouska.core.model;
	exports com.gitlab.tixtix320.jouska.core.dto;
}
