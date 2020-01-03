package com.gitlab.tixtix320.jouska.client.service;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitlab.tixtix320.jouska.client.app.Jouska;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.Player;
import com.gitlab.tixtix320.jouska.core.model.Turn;
import com.gitlab.tixtix320.sonder.api.common.rpc.Endpoint;
import com.gitlab.tixtix320.sonder.api.common.topic.Topic;

import static com.gitlab.tixtix320.jouska.client.app.Services.CLONDER;

@Endpoint("game")
public class GameEndpoint {

	@Endpoint("start")
	public void startGame(long gameId, GameBoard gameBoard, Player player, Player firstTurn, int playersCount) {
		Topic<Turn> turnTopicPublisher = CLONDER.registerTopic("game: " + gameId, new TypeReference<>() {});
		Jouska.switchScene("game",
				Map.of("board", gameBoard, "turnTopic", turnTopicPublisher, "player", player, "firstTurn", player,
						"playersCount", playersCount));
	}
}
