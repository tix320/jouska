package com.github.tix320.jouska.client.ui;

import com.github.tix320.jouska.client.app.Jouska;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public final class MenuController implements Controller {

	@FXML
	private Button joinGameButton;

	@FXML
	private Button createGameButton;

	@Override
	public void initialize(Object data) {

	}

	@FXML
	void mouseEntered(MouseEvent event) {
		((Button) event.getSource()).setScaleX(1.2);
		((Button) event.getSource()).setScaleY(1.2);
	}

	@FXML
	void mouseExited(MouseEvent event) {
		((Button) event.getSource()).setScaleX(1);
		((Button) event.getSource()).setScaleY(1);
	}

	@FXML
	void joinGame(ActionEvent event) {
		Jouska.switchScene("game-joining");
	}

	@FXML
	void createGame(ActionEvent event) {
		Jouska.switchScene("game-creating");
	}
}
