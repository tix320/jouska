package com.github.tix320.jouska.client.ui.controller;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.client.ui.controller.MenuController.ContentType;
import com.github.tix320.jouska.client.ui.tournament.TournamentItem;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.model.RoleName;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;

import static com.github.tix320.jouska.client.app.Services.TOURNAMENT_SERVICE;

public class TournamentLobbyController implements Controller<Object> {

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private FlowPane gameItemsPane;

	@FXML
	private Button createTournamentButton;

	@Override
	public void initialize(Object data) {
		gameItemsPane.disableProperty().bind(loading);
		refresh();
		loading.addListener((observable, oldValue, newValue) -> MenuController.SELF.loadingProperty().set(newValue));
	}

	@FXML
	void refresh() {
		fetchTournaments();
	}

	private void fetchTournaments() {
		RoleName role = CurrentUserContext.getPlayer().getRole();
		if(role!=RoleName.ADMIN){
			createTournamentButton.setVisible(false);
		}
		loading.set(true);
		TOURNAMENT_SERVICE.getTournaments().subscribe(tournamentViews -> {
			List<TournamentItem> tournamentItems = tournamentViews.stream()
					.map(TournamentItem::new)
					.collect(Collectors.toList());
			Collections.reverse(tournamentItems);
			tournamentItems.forEach(
					tournamentItem -> tournamentItem.setOnMouseClicked(event -> onItemClick(tournamentItem, event)));
			Platform.runLater(() -> {
				ObservableList<Node> gameList = gameItemsPane.getChildren();
				gameList.clear();
				gameList.addAll(tournamentItems);
			});
			loading.set(false);
		});
	}

	private void onItemClick(TournamentItem tournamentItem, MouseEvent event) {
		if (event.getClickCount() == 2) {
			TournamentView tournamentView = tournamentItem.getTournamentView();
			long tournamentId = tournamentView.getId();
			loading.set(true);
			TOURNAMENT_SERVICE.getStructure(tournamentId).subscribe(tournamentStructure -> {
				if (tournamentStructure != null) {
					MenuController.SELF.changeContent(ContentType.TOURNAMENT_MANAGEMENT, tournamentStructure);
				}
				else {
					JouskaUI.switchScene(ComponentType.ERROR, "Wtf. Tournament not found");
				}
			});
		}
	}

	public void createTournament(ActionEvent event) {
		MenuController.SELF.changeContent(ContentType.TOURNAMENT_CREATE);
	}
}
