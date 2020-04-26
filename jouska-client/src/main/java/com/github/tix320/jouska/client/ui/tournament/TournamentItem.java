package com.github.tix320.jouska.client.ui.tournament;

import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class TournamentItem extends AnchorPane {

	@FXML
	private Label gameNameLabel;

	@FXML
	private Label playersCountLabel;

	@FXML
	private Button joinButton;

	@FXML
	private Button viewButton;

	@FXML
	private Button startButton;

	private final TournamentView tournamentView;

	public TournamentItem(TournamentView tournamentView) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/tournament/tournament-item.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
		this.tournamentView = tournamentView;
		initView(tournamentView);
	}

	public TournamentView getTournamentView() {
		return tournamentView;
	}

	public void setOnJoinClick(EventHandler<? super MouseEvent> handler) {
		joinButton.setOnMouseClicked(handler);
	}

	public void setOnViewClick(EventHandler<? super MouseEvent> handler) {
		viewButton.setOnMouseClicked(handler);
	}

	public void setOnStartClick(EventHandler<? super MouseEvent> handler) {
		startButton.setOnMouseClicked(handler);
	}

	@FXML
	private void copyTournamentId() {
		String id = tournamentView.getId();
		ClipboardContent content = new ClipboardContent();
		content.putString(String.valueOf(id));
		Clipboard.getSystemClipboard().setContent(content);
	}

	private void initView(TournamentView tournamentView) {
		setGameName(tournamentView.getName());
		setPlayersCount(tournamentView.getPlayersCount(), tournamentView.getMaxPlayersCount());
		resolveStartButtonAccessibility(tournamentView);

		if (tournamentView.getPlayersCount() == tournamentView.getMaxPlayersCount()) {
			joinButton.setDisable(true);
		}

		if (tournamentView.isStarted()) {
			startButton.setDisable(true);
			joinButton.setDisable(true);
		}
		else {
			viewButton.setDisable(true);
		}

		if (tournamentView.getPlayersCount() < 4) {
			startButton.setDisable(true);
		}
	}

	private void setGameName(String gameName) {
		this.gameNameLabel.setText(gameName);
	}

	private void setPlayersCount(int playerCount, int maxPlayersCount) {
		this.playersCountLabel.setText(playerCount + "/" + maxPlayersCount);
	}

	private void resolveStartButtonAccessibility(TournamentView tournamentView) {
		Player creator = tournamentView.getCreator();
		Player currentPlayer = CurrentUserContext.getPlayer();

		if (!currentPlayer.equals(creator) && !currentPlayer.isAdmin()) {
			startButton.setDisable(true);
			startButton.setVisible(false);
		}
	}
}
