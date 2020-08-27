package com.github.tix320.jouska.client.app.inject;

import com.github.tix320.jouska.client.service.origin.*;
import com.github.tix320.jouska.client.ui.controller.*;
import com.github.tix320.ravel.api.Prototype;
import com.github.tix320.ravel.api.UseModules;

/**
 * @author Tigran Sargsyan on 27-Aug-20
 */
@UseModules(names = "origins")
public class ControllersModule {

	@Prototype
	public ServerConnectController serverConnectController() {
		return new ServerConnectController();
	}

	@Prototype
	public ErrorController errorController() {
		return new ErrorController();
	}

	@Prototype
	public MenuController menuController(AuthenticationOrigin authenticationOrigin) {
		return new MenuController(authenticationOrigin);
	}

	@Prototype
	public GameController gameController(ClientGameOrigin gameOrigin, ClientGameManagementOrigin gameManagementOrigin) {
		return new GameController(gameOrigin, gameManagementOrigin);
	}

	@Prototype
	public GameCreatingController gameCreatingController(ClientPlayerOrigin playerOrigin,
														 ClientGameManagementOrigin gameManagementOrigin) {
		return new GameCreatingController(playerOrigin, gameManagementOrigin);
	}

	@Prototype
	public LobbyController lobbyController(ClientGameManagementOrigin gameManagementOrigin,
										   ClientTournamentOrigin tournamentOrigin,
										   AuthenticationOrigin authenticationOrigin) {
		return new LobbyController(gameManagementOrigin, tournamentOrigin, authenticationOrigin);
	}

	@Prototype
	public LoginController loginController(AuthenticationOrigin authenticationOrigin) {
		return new LoginController(authenticationOrigin);
	}

	@Prototype
	public RegistrationController registrationController(AuthenticationOrigin authenticationOrigin) {
		return new RegistrationController(authenticationOrigin);
	}

	@Prototype
	public TournamentCreateController tournamentCreateController(ClientTournamentOrigin tournamentOrigin) {
		return new TournamentCreateController(tournamentOrigin);
	}

	@Prototype
	public TournamentLobbyController tournamentLobbyController(ClientTournamentOrigin tournamentOrigin) {
		return new TournamentLobbyController(tournamentOrigin);
	}

	@Prototype
	public TournamentViewController tournamentViewController(ClientTournamentOrigin tournamentOrigin) {
		return new TournamentViewController(tournamentOrigin);
	}

	@Prototype
	public UpdateAppController tournamentCreateController(ApplicationUpdateOrigin applicationUpdateOrigin) {
		return new UpdateAppController(applicationUpdateOrigin);
	}
}
