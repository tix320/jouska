package com.github.tix320.jouska.client.app.inject;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.service.origin.*;
import com.github.tix320.jouska.client.ui.controller.*;
import com.github.tix320.jouska.client.ui.controller.notification.GamePlayersOfflineNotificationController;
import com.github.tix320.jouska.client.ui.controller.notification.GameStartSoonNotificationController;
import com.github.tix320.jouska.client.ui.controller.notification.TournamentAcceptPlayerNotificationController;
import com.github.tix320.ravel.api.module.UseModules;
import com.github.tix320.ravel.api.scope.Prototype;

/**
 * @author Tigran Sargsyan on 27-Aug-20
 */
@UseModules(classes = CommonModule.class, dynamic = "origins")
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
	public UpdateAppController updateAppController(Configuration configuration,
												   ApplicationUpdateOrigin applicationUpdateOrigin) {
		return new UpdateAppController(configuration, applicationUpdateOrigin);
	}

	@Prototype
	public MenuController menuController(Configuration configuration, AuthenticationOrigin authenticationOrigin) {
		return new MenuController(configuration, authenticationOrigin);
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
	public LoginController loginController(Configuration configuration, AuthenticationOrigin authenticationOrigin) {
		return new LoginController(configuration, authenticationOrigin);
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
	public GamePlayersOfflineNotificationController gamePlayersOfflineNotificationController() {
		return new GamePlayersOfflineNotificationController();
	}

	@Prototype
	public TournamentAcceptPlayerNotificationController tournamentAcceptPlayerNotificationController() {
		return new TournamentAcceptPlayerNotificationController();
	}

	@Prototype
	public GameStartSoonNotificationController gameStartSoonNotificationController() {
		return new GameStartSoonNotificationController();
	}
}
