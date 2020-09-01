package com.github.tix320.jouska.server.infrastructure.entity;

import java.util.Objects;

import com.github.tix320.jouska.core.model.Role;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity("players")
public class PlayerEntity implements Identifiable {

	@Id
	private ObjectId id;

	private String nickname;

	private String password;

	private Role role;

	public PlayerEntity() {
	}

	public PlayerEntity(String id) {
		this.id = new ObjectId(id);
	}

	public PlayerEntity(String nickname, String password, Role role) {
		this.nickname = nickname;
		this.password = password;
		this.role = role;
	}

	public String getId() {
		return id.toHexString();
	}

	public String getNickname() {
		return nickname;
	}

	public String getPassword() {
		return password;
	}

	public Role getRole() {
		return role;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PlayerEntity that = (PlayerEntity) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
