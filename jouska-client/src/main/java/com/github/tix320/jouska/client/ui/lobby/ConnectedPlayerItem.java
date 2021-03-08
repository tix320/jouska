package com.github.tix320.jouska.client.ui.lobby;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.ui.helper.FXHelper;
import com.github.tix320.jouska.core.model.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ConnectedPlayerItem extends AnchorPane {

	@FXML
	private Label nicknameLabel;

	public ConnectedPlayerItem(Player player) {
		FXHelper.loadFxmlForController("/ui/lobby/connected-player-item.fxml", this);
		initView(player);
	}

	private void initView(Player player) {
		setNickname(player.getNickname());

	}

	private void setNickname(String nickname) {
		this.nicknameLabel.setText(nickname);
	}

}
