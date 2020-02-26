package com.github.tix320.jouska.client.ui;

import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;

public final class MenuController implements Controller {

	public static MenuController SELF;

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private AnchorPane contentPane;

	public MenuController() {
		SELF = this;
	}

	@Override
	public void initialize(Object data) {
		JouskaUI.changeMenuScene("lobby");
		loadingIndicator.visibleProperty().bind(loading);
	}

	@FXML
	void joinGame(ActionEvent event) {
		JouskaUI.changeMenuScene("lobby");
	}

	@FXML
	void createGame(ActionEvent event) {
		JouskaUI.changeMenuScene("game-creating");
	}

	public void changeContent(Node node) {
		ObservableList<Node> children = this.contentPane.getChildren();
		children.clear();
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		children.add(node);
	}

	public SimpleBooleanProperty loadingProperty() {
		return loading;
	}
}
