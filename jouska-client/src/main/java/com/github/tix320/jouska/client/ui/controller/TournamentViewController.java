package com.github.tix320.jouska.client.ui.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.tix320.jouska.client.infrastructure.UI;
import com.github.tix320.jouska.client.infrastructure.UI.ComponentType;
import com.github.tix320.jouska.client.ui.tournament.GroupPane;
import com.github.tix320.jouska.client.ui.tournament.PlayOffMember;
import com.github.tix320.jouska.core.dto.PlayOffGameView;
import com.github.tix320.jouska.core.dto.TournamentStructure;
import com.github.tix320.jouska.core.dto.TournamentStructure.GroupPlayerView;
import com.github.tix320.jouska.core.dto.TournamentStructure.GroupView;
import com.github.tix320.jouska.core.dto.TournamentStructure.PlayOffView;
import com.github.tix320.jouska.core.model.Player;
import com.github.tix320.jouska.core.util.MathUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import static com.github.tix320.jouska.client.app.Services.TOURNAMENT_SERVICE;

public class TournamentViewController implements Controller<TournamentStructure> {

	private static final int SUPPORTED_PLAYERS_COUNT = 16;
	private static final int SUPPORTED_PLAY_OFF_PLAYERS_COUNT = 8;

	@FXML
	private HBox groupsBox;

	@FXML
	private HBox playOffPane;

	@Override
	public void init(TournamentStructure tournamentStructure) {
		initView(tournamentStructure);

		Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
			TOURNAMENT_SERVICE.getTournamentStructure(tournamentStructure.getId()).subscribe(newStructure -> {
				if (newStructure != null) {
					Platform.runLater(() -> {
						groupsBox.getChildren().clear();
						playOffPane.getChildren().clear();
						initView(newStructure);
					});
				}
				else {
					UI.switchComponent(ComponentType.ERROR, "Tournament not found");
				}
			});
		}, 5, 5, TimeUnit.SECONDS);
	}

	@Override
	public void destroy() {

	}

	private void initView(TournamentStructure tournamentStructure) {
		fillPlayOffNodes();
		initStructure(tournamentStructure);
	}

	private void fillPlayOffNodes() {
		AnchorPane firstColumnsPane = createFirstColumnsPane();
		AnchorPane secondColumnsPane = createSecondColumnsPane();
		AnchorPane thirdColumnsPane = createThirdColumnsPane();
		AnchorPane winnerColumnPane = createWinnerColumnPane();
		playOffPane.getChildren().addAll(firstColumnsPane, secondColumnsPane, thirdColumnsPane, winnerColumnPane);
	}


	private void initStructure(TournamentStructure tournamentStructure) {
		int playersCount = tournamentStructure.getGroups()
				.stream()
				.mapToInt(groupView -> groupView.getPlayerViews().size())
				.sum();

		if (playersCount > SUPPORTED_PLAYERS_COUNT) {
			throw new IllegalArgumentException(
					"View does not support tournament players count greater than " + SUPPORTED_PLAYERS_COUNT);
		}

		initGroupView(tournamentStructure.getGroups());

		PlayOffView playOffView = tournamentStructure.getPlayOffView();
		if (playOffView != null) {
			if (playOffView.getPlayersCount() > SUPPORTED_PLAY_OFF_PLAYERS_COUNT) {
				throw new IllegalArgumentException("View does not support play-off players count greater than "
												   + SUPPORTED_PLAY_OFF_PLAYERS_COUNT);

			}
			initPlayOffView(playOffView);
		}
	}

	private void initGroupView(List<GroupView> groups) {
		for (GroupView group : groups) {
			group.getPlayerViews()
					.sort(Comparator.comparing(GroupPlayerView::getGroupPoints)
							.thenComparing(GroupPlayerView::getGamesPoints)
							.reversed());
			GroupPane groupPane = new GroupPane(group);
			groupsBox.getChildren().add(groupPane);
		}
	}

	private void initPlayOffView(PlayOffView playOffView) {
		int toursCount = (int) MathUtils.log2(SUPPORTED_PLAY_OFF_PLAYERS_COUNT);

		fillGapsInTours(playOffView.getTours(), toursCount);

		List<List<PlayOffGameView>> tours = playOffView.getTours();
		for (int tourIndex = 0; tourIndex < tours.size(); tourIndex++) {
			List<PlayOffGameView> tourGames = tours.get(tourIndex);

			for (int j = 0; j < tourGames.size(); j++) {
				PlayOffGameView game = tourGames.get(j);
				int memberNodeIndex = j * 2 + 1;
				fillTwoPLayerGameView(game, tourIndex, memberNodeIndex);
			}
		}

		if (playOffView.getWinner() != null) {
			fillWinner(playOffView.getWinner());
		}
	}

	private void fillGapsInTours(List<List<PlayOffGameView>> tours, int toursToFill) {
		while (toursToFill != tours.size()) {
			List<Player> allPLayersOfFirstTour = tours.get(0)
					.stream()
					.flatMap(playOffGameView -> Stream.of(playOffGameView.getFirstPlayer(),
							playOffGameView.getSecondPlayer()))
					.collect(Collectors.toList());

			List<PlayOffGameView> missingTourGameViews = new ArrayList<>();
			for (Player player : allPLayersOfFirstTour) {
				missingTourGameViews.add(new PlayOffGameView(player, null, 1));
			}
			tours.add(0, missingTourGameViews);
		}
	}

	private void fillTwoPLayerGameView(PlayOffGameView playOffGameView, int tourIndex, int memberIndex) {
		AnchorPane tourPane = (AnchorPane) playOffPane.getChildren().get(tourIndex);
		PlayOffMember firstMember = (PlayOffMember) tourPane.getChildren().get(memberIndex);
		PlayOffMember secondMember = (PlayOffMember) tourPane.getChildren().get(memberIndex + 1);
		firstMember.setNickname(
				playOffGameView.getFirstPlayer() == null ? "--" : playOffGameView.getFirstPlayer().getNickname());
		secondMember.setNickname(
				playOffGameView.getSecondPlayer() == null ? "--" : playOffGameView.getSecondPlayer().getNickname());
		if (playOffGameView.getWinner() != -1) {
			if (playOffGameView.getWinner() == 1) {
				firstMember.getStyleClass().add("playOffGameWinner");
				secondMember.getStyleClass().add("playOffGameLoser");
			}
			else {
				firstMember.getStyleClass().add("playOffGameLoser");
				secondMember.getStyleClass().add("playOffGameWinner");
			}
		}
		else {
			if (playOffGameView.getFirstPlayer() != null) {
				firstMember.getStyleClass().add("playOffMemberInProgress");
			}
			if (playOffGameView.getSecondPlayer() != null) {
				secondMember.getStyleClass().add("playOffMemberInProgress");
			}
		}
	}

	private void fillWinner(Player winner) {
		int winnerTourIndex = (int) MathUtils.log2(SUPPORTED_PLAY_OFF_PLAYERS_COUNT);
		AnchorPane tourPane = (AnchorPane) playOffPane.getChildren().get(winnerTourIndex);
		PlayOffMember winnerNode = (PlayOffMember) tourPane.lookup(".playOffMember");
		winnerNode.setNickname(winner.getNickname());
		winnerNode.getStyleClass().add("playOffWinner");
	}

	private AnchorPane createFirstColumnsPane() {
		double[] anchorPaneTopLayouts = new double[]{
				40, 90, 160, 210, 270, 320, 390, 440};
		Label label = createLabel("1/8 Final");
		AnchorPane anchorPane = new AnchorPane(label);
		for (int i = 0; i < 8; i++) {
			Pane pane = createMemberPane(anchorPaneTopLayouts[i]);
			anchorPane.getChildren().add(pane);
		}
		return anchorPane;
	}

	private AnchorPane createSecondColumnsPane() {
		double[] anchorPaneTopLayouts = new double[]{
				65, 185, 295, 415,};
		Label label = createLabel("1/4 Final");
		AnchorPane anchorPane = new AnchorPane(label);
		for (int i = 0; i < 4; i++) {
			Pane pane = createMemberPane(anchorPaneTopLayouts[i]);
			anchorPane.getChildren().add(pane);
		}
		return anchorPane;
	}

	private AnchorPane createThirdColumnsPane() {
		double[] anchorPaneTopLayouts = new double[]{
				125, 355,};
		Label label = createLabel("1/2 Final");
		AnchorPane anchorPane = new AnchorPane(label);
		for (int i = 0; i < 2; i++) {
			Pane pane = createMemberPane(anchorPaneTopLayouts[i]);
			anchorPane.getChildren().add(pane);
		}
		return anchorPane;
	}

	private AnchorPane createWinnerColumnPane() {
		Label label = createLabel("Winner");
		Pane pane = createMemberPane(240D);
		return new AnchorPane(label, pane);
	}

	private Label createLabel(String text) {
		Label label = new Label(text);
		label.getStyleClass().add("playOffTourLabel");
		AnchorPane.setLeftAnchor(label, 10D);
		AnchorPane.setRightAnchor(label, 10D);
		AnchorPane.setTopAnchor(label, 0D);
		return label;
	}

	private Pane createMemberPane(double topMargin) {
		PlayOffMember pane = new PlayOffMember();
		AnchorPane.setTopAnchor(pane, topMargin);
		AnchorPane.setLeftAnchor(pane, 0D);
		AnchorPane.setRightAnchor(pane, 0D);
		pane.getStyleClass().add("playOffMember");
		pane.setNickname("--");
		return pane;
	}
}
