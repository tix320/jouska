open module jouska.core {
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;
	requires kiwi;

	exports com.github.tix320.jouska.core.model;
	exports com.github.tix320.jouska.core.dto;
	exports com.github.tix320.jouska.core.game;
}
