package com.github.tix320.jouska.client.ui.controller;

import java.util.List;

import com.github.tix320.jouska.client.ui.tournament.GroupPane;
import com.github.tix320.jouska.core.dto.TournamentStructure;
import com.github.tix320.jouska.core.dto.TournamentStructure.GroupView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class TournamentManagementController implements Controller<TournamentStructure> {

	@FXML
	private HBox groupsBox;

	@FXML
	private HBox playOffPane;

	@Override
	public void init(TournamentStructure tournamentStructure) {
		AnchorPane firstColumnsPane = createFirstColumnsPane();
		AnchorPane secondColumnsPane = createSecondColumnsPane();
		AnchorPane thirdColumnsPane = createThirdColumnsPane();
		AnchorPane winnerColumnPane = createWinnerColumnPane();
		playOffPane.getChildren().addAll(firstColumnsPane, secondColumnsPane, thirdColumnsPane, winnerColumnPane);
		initStructure(tournamentStructure);
	}

	@Override
	public void destroy() {

	}

	private void initStructure(TournamentStructure tournamentStructure) {
		List<GroupView> groups = tournamentStructure.getGroups();
		for (GroupView group : groups) {
			GroupPane groupPane = new GroupPane(group);
			groupsBox.getChildren().add(groupPane);
		}
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
		label.getStyleClass().add("playOffLabel");
		AnchorPane.setLeftAnchor(label, 10D);
		AnchorPane.setRightAnchor(label, 10D);
		AnchorPane.setTopAnchor(label, 0D);
		return label;
	}

	private Pane createMemberPane(double topMargin) {
		Pane pane = new Pane();
		AnchorPane.setTopAnchor(pane, topMargin);
		AnchorPane.setLeftAnchor(pane, 0D);
		AnchorPane.setRightAnchor(pane, 0D);
		pane.getStyleClass().add("playOffMember");
		return pane;
	}
}
