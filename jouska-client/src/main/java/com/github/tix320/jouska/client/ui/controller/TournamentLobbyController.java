package com.github.tix320.jouska.client.ui.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.jouska.client.ui.controller.MenuController.MenuContentType;
import com.github.tix320.jouska.client.ui.tournament.TournamentItem;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.core.model.RoleName;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

import static com.github.tix320.jouska.client.app.Services.TOURNAMENT_SERVICE;

public class TournamentLobbyController implements Controller<Object> {

	@FXML
	private FlowPane gameItemsPane;

	@FXML
	private Button createTournamentButton;

	private MonoPublisher<None> destroyPublisher = Publisher.mono();

	@Override
	public void init(Object data) {
		subscribeToTournaments();
	}

	@Override
	public void destroy() {
		destroyPublisher.complete();
	}

	private void subscribeToTournaments() {
		RoleName role = CurrentUserContext.getPlayer().getRole();
		if (role != RoleName.ADMIN) {
			createTournamentButton.setVisible(false);
		}
		TOURNAMENT_SERVICE.getTournaments().takeUntil(destroyPublisher.asObservable()).subscribe(tournamentViews -> {
			List<TournamentItem> tournamentItems = tournamentViews.stream()
					.map(TournamentItem::new)
					.collect(Collectors.toList());
			Collections.reverse(tournamentItems);
			tournamentItems.forEach(tournamentItem -> {
				tournamentItem.setOnJoinClick(event -> joinTournament(tournamentItem));
				tournamentItem.setOnViewClick(event -> viewTournament(tournamentItem));
				tournamentItem.setOnStartClick(event -> startTournament(tournamentItem));
			});
			Platform.runLater(() -> {
				ObservableList<Node> gameList = gameItemsPane.getChildren();
				gameList.clear();
				gameList.addAll(tournamentItems);
			});
		});
	}

	private void joinTournament(TournamentItem tournamentItem) {
		TournamentView tournamentView = tournamentItem.getTournamentView();
		long tournamentId = tournamentView.getId();
		TOURNAMENT_SERVICE.join(tournamentId).subscribe(System.out::println);
	}

	private void viewTournament(TournamentItem tournamentItem) {
		TournamentView tournamentView = tournamentItem.getTournamentView();
		long tournamentId = tournamentView.getId();
		TOURNAMENT_SERVICE.getTournamentStructure(tournamentId).subscribe(tournamentStructure -> {
			if (tournamentStructure != null) {
				EventDispatcher.fire(
						new MenuContentChangeEvent(MenuContentType.TOURNAMENT_MANAGEMENT, tournamentStructure));
			}
			else {
				UI.switchComponent(ComponentType.ERROR, "Tournament not found");
			}
		});
	}

	private void startTournament(TournamentItem tournamentItem) {
		TOURNAMENT_SERVICE.startTournament(tournamentItem.getTournamentView().getId());
	}


	public void createTournament() {
		EventDispatcher.fire(new MenuContentChangeEvent(MenuContentType.TOURNAMENT_CREATE));
	}
}
