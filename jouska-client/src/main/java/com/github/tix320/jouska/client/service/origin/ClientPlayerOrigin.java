package com.github.tix320.jouska.client.service.origin;

import java.util.List;

import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;

/**
 * @author tigra on 05-Apr-20.
 */
@Origin("player")
public interface ClientPlayerOrigin {

	@Origin
	MonoObservable<List<Player>> getPlayersByNickname(List<String> nicknames);
}
