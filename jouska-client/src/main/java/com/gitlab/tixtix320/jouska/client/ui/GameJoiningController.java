package com.gitlab.tixtix320.jouska.client.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gitlab.tixtix320.jouska.client.app.Jouska;
import com.gitlab.tixtix320.jouska.core.model.GameBoard;
import com.gitlab.tixtix320.jouska.core.model.GameInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import static com.gitlab.tixtix320.jouska.client.app.Services.CLONDER;
import static com.gitlab.tixtix320.jouska.client.app.Services.GAME_SERVICE;

public class GameJoiningController implements Controller {

    private final SimpleBooleanProperty loading = new SimpleBooleanProperty(false);

    @FXML
    private TableView<GameInfo> gamesTable;

    @FXML
    private ProgressIndicator loadingIndicator;

    @FXML
    private TableColumn<GameInfo, Long> idColumn;

    @FXML
    private TableColumn<GameInfo, String> nameColumn;

    @FXML
    private TableColumn<GameInfo, String> playersColumn;

    @FXML
    private Button refreshButton;

    @Override
    public void initialize(Object data) {
        refreshButton.disableProperty().bind(loading);
        gamesTable.disableProperty().bind(loading);
        loadingIndicator.visibleProperty().bind(loading);
        refreshButton.setGraphic(new ImageView("ui/game-joining/refresh.png"));
        initGamesTable();
        fetchGameInfos();
    }

    @FXML
    void refresh(ActionEvent event) {
        fetchGameInfos();
    }

    public void initGamesTable() {
        idColumn.setCellValueFactory(cell -> new SimpleLongProperty(cell.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        playersColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPlayers() + "/" + cell.getValue().getPlayers()));

        gamesTable.setRowFactory(param -> {
            TableRow<GameInfo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    GameInfo gameInfo = row.getItem();
                    long gameId = gameInfo.getId();
                    loading.set(true);
                    CLONDER.registerTopicPublisher("game-board: " + gameId, new TypeReference<GameBoard>() {
                    }).asObservable().subscribe(gameBoard -> {
                        Platform.runLater(() -> Jouska.switchScene("game", gameBoard));
                    });
                    GAME_SERVICE.connect(gameId).subscribe(status -> {
                        if (status.equals("connected")) {
//                            Platform.runLater(() -> Jouska.switchScene("waiting"));
                        } else {
                            // show popup error
                        }
                    });
                }
            });

            return row;
        });
    }

    private void fetchGameInfos() {
        loading.set(true);
        GAME_SERVICE.getGames().subscribe(gameInfos -> {
            gamesTable.setItems(FXCollections.observableArrayList(gameInfos));
            loading.set(false);
        });
    }


}
