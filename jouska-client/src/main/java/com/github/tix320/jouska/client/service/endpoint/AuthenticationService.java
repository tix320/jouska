package com.github.tix320.jouska.client.service.endpoint;

import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.sonder.api.common.rpc.Endpoint;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

@Endpoint("auth")
public class AuthenticationService {

	@Endpoint("logout")
	public void logout() {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("WARNING");
			alert.setHeaderText("Another logged session found.");
			alert.setContentText("Other session logged , idi naxuy.");

			alert.showAndWait();
			JouskaUI.switchScene(ComponentType.LOGIN);
		});
	}
}
