package com.github.tix320.jouska.server.infrastructure.origin;

import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.GamePlayDto;
import com.github.tix320.jouska.core.dto.GamePlayersOfflineWarning;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

@Origin("game")
public interface ServerGameManagementOrigin {

	@Origin
	void notifyGamePlayersOffline(GamePlayersOfflineWarning warning, @ClientID long clientId);

	@Origin
	MonoObservable<Confirmation> notifyGameStartingSoon(String gameName, @ClientID long clientId);

	@Origin
	void notifyGameStarted(GamePlayDto gamePlayDto, @ClientID long clientId);
}
