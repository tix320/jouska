package com.github.tix320.jouska.client.service.endpoint;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.ui.controller.LoginController;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

@Endpoint("auth")
public class AuthenticationEndpoint {

	@Endpoint("logout")
	public void logout() {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("WARNING");
			alert.setHeaderText("Another logged session found.");
			alert.setContentText("Other session logged, good bye.");

			alert.showAndWait();
			UI.switchComponent(LoginController.class);
		});
	}
}
