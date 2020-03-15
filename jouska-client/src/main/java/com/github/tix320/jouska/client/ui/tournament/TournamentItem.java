package com.github.tix320.jouska.client.ui.tournament;

import com.github.tix320.jouska.core.dto.TournamentView;
import com.github.tix320.kiwi.api.check.Try;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class TournamentItem extends AnchorPane {

	@FXML
	private Label gameNameLabel;

	@FXML
	private Label playersCountLabel;

	private final TournamentView tournamentView;

	public TournamentItem(TournamentView tournamentView) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/tournament/tournament-item.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
		this.tournamentView = tournamentView;
		initView(tournamentView);
	}

	private void initView(TournamentView tournamentView) {
		setGameName(tournamentView.getName());
		setPlayersCount(String.valueOf(tournamentView.getPlayersCount()));
	}

	private void setGameName(String gameName) {
		this.gameNameLabel.setText(gameName);
	}

	private void setPlayersCount(String playerCount) {
		this.playersCountLabel.setText(playerCount);
	}

	public TournamentView getTournamentView() {
		return tournamentView;
	}

}
