package com.github.tix320.jouska.server.infrastructure.origin;

import com.github.tix320.jouska.core.dto.Confirmation;
import com.github.tix320.jouska.core.dto.TournamentJoinRequest;
import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.sonder.api.common.rpc.Origin;
import com.github.tix320.sonder.api.common.rpc.extra.ClientID;

/**
 * @author Tigran Sargsyan on 29-Mar-20.
 */
@Origin("tournament")
public interface ServerTournamentOrigin {

	@Origin
	MonoObservable<Confirmation> requestTournamentJoin(TournamentJoinRequest request, @ClientID long clientId);
}
