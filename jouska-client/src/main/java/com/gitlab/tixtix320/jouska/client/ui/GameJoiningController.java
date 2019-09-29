package com.gitlab.tixtix320.jouska.client.ui;

import com.gitlab.tixtix320.jouska.client.app.Services;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import ui.model.GameInfo;

public class GameJoiningController implements Controller {

    private final Scene scene;

    private final AnchorPane root;

    private final Button refreshButton;

    private final TableView<GameInfo> gamesTable;

    public GameJoiningController() {
        refreshButton = createRefreshButton();
        gamesTable = createGamesTable();
        root = new AnchorPane();
        root.getChildren().addAll(refreshButton, gamesTable);
        scene = new Scene(root, 300, 300);
        fetchGameInfos();
    }

    public Button createRefreshButton() {
        Button button = new Button();
        button.setGraphic(new ImageView("ui/game-joining/refresh.png"));
        button.setOnMouseClicked(event -> {
            fetchGameInfos();
        });
        return button;
    }

    public TableView<GameInfo> createGamesTable() {
        TableView<GameInfo> tableView = new TableView<>();
        tableView.setRowFactory(param -> {
            TableRow<GameInfo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    GameInfo gameInfo = row.getItem();
                    Meduzon.switchController(new WaitingController(gameInfo));
                }
            });

            return row;
        });
        TableColumn<GameInfo, Integer> id = new TableColumn<>("Id");
        TableColumn<GameInfo, String> name = new TableColumn<>("Name");
        TableColumn<GameInfo, String> players = new TableColumn<>("Players");
        id.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getId()).asObject());
        name.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        players.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPlayers() + "/" + cell.getValue().getPlayers()));
        tableView.getColumns().add(id);
        tableView.getColumns().add(name);
        tableView.getColumns().add(players);
        return tableView;
    }

    private void fetchGameInfos() {
        Services.GAME_INFO_SERVICE.getGames().subscribe(gameInfos -> gamesTable.setItems(FXCollections.observableArrayList(gameInfos)));
    }

    @Override
    public Scene getOwnScene() {
        return scene;
    }

    @Override
    public void initialize() {

    }
}
