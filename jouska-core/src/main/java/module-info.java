open module jouska.core {
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires com.fasterxml.jackson.databind;

	requires com.github.tix.kiwi;
	requires com.github.tix.skimp;

	requires io.github.classgraph;

	exports com.github.tix320.jouska.core;
	exports com.github.tix320.jouska.core.model;
	exports com.github.tix320.jouska.core.dto;
	exports com.github.tix320.jouska.core.application.game;
	exports com.github.tix320.jouska.core.application.game.creation;
	exports com.github.tix320.jouska.core.application.tournament;
	exports com.github.tix320.jouska.core.event;
	exports com.github.tix320.jouska.core.util;
	exports com.github.tix320.jouska.core.infrastructure;
}
