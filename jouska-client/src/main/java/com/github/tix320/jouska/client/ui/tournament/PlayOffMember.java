package com.github.tix320.jouska.client.ui.tournament;

import com.github.tix320.jouska.client.ui.helper.FXHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

/**
 * @author Tigran Sargsyan on 06-Apr-20.
 */
public class PlayOffMember extends AnchorPane {

	@FXML
	private Label nicknameLabel;

	public PlayOffMember() {
		FXHelper.loadFxmlForController("/ui/tournament/play-off-member.fxml", this);
	}

	public void setNickname(String nickname) {
		this.nicknameLabel.setText(nickname);
	}
}
