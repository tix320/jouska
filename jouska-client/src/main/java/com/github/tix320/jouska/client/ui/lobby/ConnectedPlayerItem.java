package com.github.tix320.jouska.client.ui.lobby;

import com.github.tix320.jouska.core.dto.GameView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.kiwi.api.check.Try;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ConnectedPlayerItem extends AnchorPane {
	@FXML
	private Label nickname;
	private Player player;

	public ConnectedPlayerItem(Player player) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/lobby/connected-player-item.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		Try.runOrRethrow(fxmlLoader::load);
		this.player = player;
		initView(player);
	}

	private void initView(Player player) {
		setNickname(player.getNickname());

	}

	private void setNickname(String nickname) {
		this.nickname.setText(nickname);
	}

}
