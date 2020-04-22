package com.github.tix320.jouska.core.dto;

import java.util.List;

import com.github.tix320.jouska.core.model.Player;

public class TournamentStructure {

	private final String id;

	private final List<GroupView> groups;

	private final PlayOffView playOffView;

	private TournamentStructure() {
		this(null, null, null);
	}

	public TournamentStructure(String id, List<GroupView> groups, PlayOffView playOffView) {
		this.id = id;
		this.groups = groups;
		this.playOffView = playOffView;
	}

	public String getId() {
		return id;
	}

	public List<GroupView> getGroups() {
		return groups;
	}

	public PlayOffView getPlayOffView() {
		return playOffView;
	}

	public static class GroupView {
		private final String groupName;
		private final List<GroupPlayerView> playerViews;

		private GroupView() {
			this(null, null);
		}

		public GroupView(String groupName, List<GroupPlayerView> playerViews) {
			this.groupName = groupName;
			this.playerViews = playerViews;
		}

		public String getGroupName() {
			return groupName;
		}

		public List<GroupPlayerView> getPlayerViews() {
			return playerViews;
		}
	}

	public static class PlayOffView {
		private final int playersCount;
		private final List<List<PlayOffGameView>> tours;
		private final Player winner;

		private PlayOffView() {
			this(-1, null, null);
		}

		public PlayOffView(int playersCount, List<List<PlayOffGameView>> tours, Player winner) {
			this.playersCount = playersCount;
			this.tours = tours;
			this.winner = winner;
		}

		public int getPlayersCount() {
			return playersCount;
		}

		public List<List<PlayOffGameView>> getTours() {
			return tours;
		}

		public Player getWinner() {
			return winner;
		}
	}

	public static class GroupPlayerView {
		private final Player player;
		private final int groupPoints;
		private final int gamesPoints;

		private GroupPlayerView() {
			this(null, -1, -1);
		}

		public GroupPlayerView(Player player, int groupPoints, int gamesPoints) {
			this.player = player;
			this.groupPoints = groupPoints;
			this.gamesPoints = gamesPoints;
		}

		public Player getPlayer() {
			return player;
		}

		public int getGroupPoints() {
			return groupPoints;
		}

		public int getGamesPoints() {
			return gamesPoints;
		}
	}
}
