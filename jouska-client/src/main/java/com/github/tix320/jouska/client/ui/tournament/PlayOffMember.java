package com.github.tix320.jouska.client.ui.tournament;

import com.github.tix320.skimp.api.check.Try;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * @author Tigran Sargsyan on 06-Apr-20.
 */
public class PlayOffMember extends AnchorPane {

	@FXML
	private Label nicknameLabel;

	public PlayOffMember() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/tournament/play-off-member.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
	}

	public void setNickname(String nickname) {
		this.nicknameLabel.setText(nickname);
	}
}
