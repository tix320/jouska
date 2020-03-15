package com.github.tix320.jouska.core.dto;

import java.util.List;
import java.util.Set;

import com.github.tix320.jouska.core.model.Player;

public class TournamentStructure {

	private final List<GroupView> groups;

	private TournamentStructure() {
		this(null);
	}

	public TournamentStructure(List<GroupView> groups) {
		this.groups = groups;
	}

	public List<GroupView> getGroups() {
		return groups;
	}

	public static class GroupView {
		private final Set<Player> players;

		private GroupView() {
			this(null);
		}

		public GroupView(Set<Player> players) {
			this.players = players;
		}

		public Set<Player> getPlayers() {
			return players;
		}
	}
}
