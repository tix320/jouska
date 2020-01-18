package com.github.tix320.jouska.client.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class WaitingController implements Controller<Long> {

	@FXML
	private AnchorPane root;

	@FXML
	private Label infoLabel;

	@FXML
	private Button cancelButton;

	@Override
	public void initialize(Long gameId) {

	}

	@FXML
	void cancel(ActionEvent event) {

	}
}
