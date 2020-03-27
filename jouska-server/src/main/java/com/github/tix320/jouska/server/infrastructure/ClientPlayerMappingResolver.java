package com.github.tix320.jouska.server.infrastructure;

import java.util.Optional;

import com.github.tix320.kiwi.api.util.collection.BiConcurrentHashMap;
import com.github.tix320.kiwi.api.util.collection.BiMap;

/**
 * @author Tigran Sargsyan on 22-Mar-20.
 */
public final class ClientPlayerMappingResolver {

	private final static BiMap<Long, String> playerAndClientIds = new BiConcurrentHashMap<>();

	public static Optional<String> getPlayerIdByClientId(long clientId) {
		return Optional.ofNullable(playerAndClientIds.straightView().get(clientId));
	}

	public static Optional<Long> getClientIdByPlayer(String playerId) {
		return Optional.ofNullable(playerAndClientIds.inverseView().get(playerId));
	}

	public static void setMapping(long clientId, String playerId) {
		playerAndClientIds.put(clientId, playerId);
	}

	public static String removeByClientId(long clientId) {
		return playerAndClientIds.straightRemove(clientId);
	}


	public static Long removeByPlayerId(String playerId) {
		return playerAndClientIds.inverseRemove(playerId);
	}
}
