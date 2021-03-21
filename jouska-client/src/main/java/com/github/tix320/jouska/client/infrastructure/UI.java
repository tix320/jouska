package com.github.tix320.jouska.client.infrastructure;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.github.tix320.jouska.client.app.AppConfig;
import com.github.tix320.jouska.client.infrastructure.event.GameStartedEvent;
import com.github.tix320.jouska.client.ui.controller.*;
import com.github.tix320.jouska.client.ui.controller.notification.GamePlayersOfflineNotificationController;
import com.github.tix320.jouska.client.ui.controller.notification.GameStartSoonNotificationController;
import com.github.tix320.jouska.client.ui.controller.notification.TournamentAcceptPlayerNotificationController;
import com.github.tix320.jouska.core.Version;
import com.github.tix320.jouska.core.event.EventDispatcher;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.ImageCursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class UI {

	public static Stage stage;

	public static Component currentComponent;

	public static void initialize(Stage stage) {
		if (UI.stage == null) {
			stage.setMinWidth(400);
			stage.setMinHeight(400);
			UI.stage = stage;
			stage.getIcons().add(new Image(UI.class.getResourceAsStream("/installer.ico")));
			stage.setTitle("Jouska " + Version.CURRENT);
			normalize();
		} else {
			throw new IllegalStateException("Application already initialized");
		}

		// TODO this is not place for this
		EventDispatcher.on(GameStartedEvent.class)
				.subscribe(event -> UI.switchComponent(GameController.class, event.getGamePlayDto()));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <C extends Controller<?>> MonoObservable<C> switchComponent(Class<C> controllerClass) {
		return switchComponent((Class<? extends Controller>) controllerClass, null);
	}

	public static <T, C extends Controller<T>> MonoObservable<C> switchComponent(Class<C> controllerClass, T data) {
		Component component = loadComponent(controllerClass, data);
		if (currentComponent != null) {
			currentComponent.getController().destroy();
		}
		currentComponent = component;
		Scene scene = new Scene(component.getRoot());

		MonoPublisher<C> switchCompletePublisher = Publisher.mono();

		Platform.runLater(() -> {
			stage.setScene(scene);
			scene.setCursor(new ImageCursor(new Image(UI.class.getResourceAsStream("/images/cursor.jpg"))));
			normalize();
			stage.centerOnScreen();
			@SuppressWarnings("unchecked")
			final C controller = (C) component.getController();
			switchCompletePublisher.publish(controller);
		});

		return switchCompletePublisher.asObservable();
	}

	public static void normalize() {
		stage.sizeToScene();

		double stageWidth = stage.getWidth();
		double stageHeight = stage.getHeight();

		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double screenWidth = screenBounds.getWidth();
		double screenHeight = screenBounds.getHeight();

		// keep 70% of screen, when larger
		stage.setWidth(Math.min(stageWidth, screenWidth * 0.7));
		stage.setHeight(Math.min(stageHeight, screenHeight * 0.7));

		stage.centerOnScreen();
	}

	public static <T, C extends Controller<? extends T>> Component loadComponent(Class<C> controllerClass, T data) {
		FXMLLoader loader = loadFxml(controllerClass);
		Parent root = loader.getRoot();

		Controller<Object> controller = loader.getController();
		controller.init(data);
		return new ComponentImpl(root, controller);
	}

	private static <C extends Controller<?>> FXMLLoader loadFxml(Class<C> controllerClass) {
		String resourceUrl = fxmlByControllers.get(controllerClass);
		URL resource = UI.class.getResource(resourceUrl);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("Fxml %s not found", resourceUrl));
		}
		FXMLLoader loader = new FXMLLoader(resource);
		loader.setControllerFactory(clazz -> AppConfig.INJECTOR.inject(clazz));
		try {
			loader.load();
		} catch (IOException e) {
			throw new IllegalArgumentException("Scene for %s not found".formatted(controllerClass.getSimpleName()), e);
		}

		return loader;
	}

	@SuppressWarnings({"rawtypes"})
	private static final Map<Class<? extends Controller>, String> fxmlByControllers = new HashMap<>();

	static {
		fxmlByControllers.put(ServerConnectController.class, "/ui/server-connect/server-connect.fxml");
		fxmlByControllers.put(UpdateAppController.class, "/ui/update-app/update-app.fxml");
		fxmlByControllers.put(LoginController.class, "/ui/auth/login.fxml");
		fxmlByControllers.put(RegistrationController.class, "/ui/auth/registration.fxml");
		fxmlByControllers.put(ErrorController.class, "/ui/error/error.fxml");
		fxmlByControllers.put(MenuController.class, "/ui/menu/menu.fxml");
		fxmlByControllers.put(GameCreatingController.class, "/ui/game-creating/game-creating.fxml");
		fxmlByControllers.put(LobbyController.class, "/ui/lobby/lobby.fxml");
		fxmlByControllers.put(TournamentLobbyController.class, "/ui/tournament/tournament-lobby.fxml");
		fxmlByControllers.put(TournamentCreateController.class, "/ui/tournament/tournament-create.fxml");
		fxmlByControllers.put(TournamentViewController.class, "/ui/tournament/tournament-view.fxml");
		fxmlByControllers.put(GameController.class, "/ui/game/game.fxml");
		fxmlByControllers.put(TournamentAcceptPlayerNotificationController.class,
				"/ui/menu/notification/confirm-notification.fxml");
		fxmlByControllers.put(GameStartSoonNotificationController.class,
				"/ui/menu/notification/confirm-notification.fxml");
		fxmlByControllers.put(GamePlayersOfflineNotificationController.class,
				"/ui/menu/notification/warning-notification.fxml");
	}

	private static final class ComponentImpl implements Component {

		private final Parent root;

		private final Controller<?> controller;

		private ComponentImpl(Parent root, Controller<?> controller) {
			this.root = root;
			this.controller = controller;
		}

		@Override
		public Parent getRoot() {
			return root;
		}

		@Override
		public Controller<?> getController() {
			return controller;
		}
	}
}
