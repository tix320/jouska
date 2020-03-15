package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;

import static com.github.tix320.jouska.client.app.Services.AUTHENTICATION_SERVICE;
import static com.github.tix320.jouska.client.app.Services.PLAYER_SERVICE;

public final class MenuController implements Controller<Object> {

	public static MenuController SELF;

	private ContentType currentContent;

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private Label nicknameLabel;

	@FXML
	private AnchorPane contentPane;

	public MenuController() {
		SELF = this;
	}

	@Override
	public void initialize(Object data) {
		changeContent(ContentType.LOBBY);
		loadingIndicator.visibleProperty().bind(loading);
		PLAYER_SERVICE.me().subscribe(player -> {
			CurrentUserContext.setPlayer(player);
			Platform.runLater(() -> nicknameLabel.setText(player.getNickname()));
		});
	}

	@FXML
	void toLobby(ActionEvent event) {
		changeContent(ContentType.LOBBY);
	}

	@FXML
	void createGame(ActionEvent event) {
		changeContent(ContentType.CREATE_GAME);
	}

	@FXML
	public void toTournament(ActionEvent event) {
		changeContent(ContentType.TOURNAMENT_LOBBY);
	}

	public void changeContent(ContentType contentType) {
		changeContent(contentType, null);
	}

	public void changeContent(ContentType contentType, Object data) {
		if (contentType == currentContent) {
			return;
		}
		Parent content = JouskaUI.loadFxml(contentType.getComponentType(), data);

		Platform.runLater(() -> {
			AnchorPane.setTopAnchor(content, 0.0);
			AnchorPane.setRightAnchor(content, 0.0);
			AnchorPane.setLeftAnchor(content, 0.0);
			AnchorPane.setBottomAnchor(content, 0.0);
			ObservableList<Node> children = this.contentPane.getChildren();
			children.clear();
			children.add(content);

			currentContent = contentType;
		});
	}

	public SimpleBooleanProperty loadingProperty() {
		return loading;
	}

	public void logout(ActionEvent event) {
		CurrentUserContext.setPlayer(null);
		AUTHENTICATION_SERVICE.logout();
		Configuration.updateCredentials("", "");
		JouskaUI.switchScene(ComponentType.LOGIN);
	}

	public enum ContentType {
		CREATE_GAME {
			@Override
			ComponentType getComponentType() {
				return ComponentType.CREATE_GAME;
			}
		},
		LOBBY {
			@Override
			ComponentType getComponentType() {
				return ComponentType.LOBBY;
			}
		},
		TOURNAMENT_LOBBY {
			@Override
			ComponentType getComponentType() {
				return ComponentType.TOURNAMENT_LOBBY;
			}
		},
		TOURNAMENT_CREATE {
			@Override
			ComponentType getComponentType() {
				return ComponentType.TOURNAMENT_CREATE;
			}
		},
		TOURNAMENT_MANAGEMENT {
			@Override
			ComponentType getComponentType() {
				return ComponentType.TOURNAMENT_MANAGEMENT;
			}
		};

		abstract ComponentType getComponentType();
	}
}
