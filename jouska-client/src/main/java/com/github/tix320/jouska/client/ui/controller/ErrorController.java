package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ErrorController implements Controller<String> {

	@FXML
	TextArea errorTextArea;

	@Override
	public void initialize(String error) {
		errorTextArea.setText(error);
	}

	public void returnToGame(ActionEvent event) {
		JouskaUI.switchScene(ComponentType.MENU);
	}
}
