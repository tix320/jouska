package com.github.tix320.jouska.client.ui.controller;

import com.github.tix320.jouska.client.app.Configuration;
import com.github.tix320.jouska.client.infrastructure.CurrentUserContext;
import com.github.tix320.jouska.client.infrastructure.JouskaUI;
import com.github.tix320.jouska.client.infrastructure.JouskaUI.ComponentType;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.jouska.client.infrastructure.event.MenuContentChangeEvent;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
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

public final class MenuController implements Controller<Object> {

	@FXML
	private ProgressIndicator loadingIndicator;

	@FXML
	private Label nicknameLabel;

	@FXML
	private AnchorPane contentPane;

	private MenuContentType currentMenuContentType;

	private Component currentComponent;

	private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

	private MonoPublisher<None> destroyPublisher = Publisher.mono();

	@Override
	public void init(Object data) {
		changeContent(MenuContentType.LOBBY);
		loadingIndicator.visibleProperty().bind(loading);
		nicknameLabel.setText(CurrentUserContext.getPlayer().getNickname());
		EventDispatcher.on(MenuContentChangeEvent.class)
				.takeUntil(destroyPublisher.asObservable())
				.subscribe(event -> changeContent(event.getMenuContentType()));
	}

	@Override
	public void destroy() {
		currentComponent.getController().destroy();
		destroyPublisher.complete();
	}

	@FXML
	void toLobby() {
		changeContent(MenuContentType.LOBBY);
	}

	@FXML
	void createGame() {
		changeContent(MenuContentType.CREATE_GAME);
	}

	@FXML
	public void toTournament() {
		changeContent(MenuContentType.TOURNAMENT_LOBBY);
	}

	private void changeContent(MenuContentType menuContentType) {
		changeContent(menuContentType, null);
	}

	public void changeContent(MenuContentType menuContentType, Object data) {
		if (menuContentType == currentMenuContentType) {
			return;
		}

		Component component = JouskaUI.loadComponent(menuContentType.getComponentType(), data);
		if (currentComponent != null) {
			currentComponent.getController().destroy();
		}

		Parent content = component.getRoot();
		Platform.runLater(() -> {
			AnchorPane.setTopAnchor(content, 0.0);
			AnchorPane.setRightAnchor(content, 0.0);
			AnchorPane.setLeftAnchor(content, 0.0);
			AnchorPane.setBottomAnchor(content, 0.0);
			ObservableList<Node> children = this.contentPane.getChildren();
			children.clear();
			children.add(content);

			currentMenuContentType = menuContentType;
			currentComponent = component;
		});
	}

	public void logout() {
		CurrentUserContext.setPlayer(null);
		AUTHENTICATION_SERVICE.logout();
		Configuration.updateCredentials("", "");
		JouskaUI.switchComponent(ComponentType.LOGIN);
	}

	public enum MenuContentType {
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
