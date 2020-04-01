package com.github.tix320.jouska.server.infrastructure;

import java.util.Map;
import java.util.Optional;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.property.BiMapProperty;
import com.github.tix320.kiwi.api.reactive.property.Property;
import com.github.tix320.kiwi.api.util.collection.BiConcurrentHashMap;
import com.github.tix320.kiwi.api.util.collection.BiMap;

/**
 * @author Tigran Sargsyan on 22-Mar-20.
 */
public final class ClientPlayerMappingResolver {

	private final static BiMapProperty<Long, String> playerAndClientIds = Property.forBiMap(
			new BiConcurrentHashMap<>());

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

	public static Observable<Map<Long, String>> getConnectedPlayers() {
		return playerAndClientIds.asObservable().map(BiMap::straightView);
	}
}
