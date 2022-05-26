package pl.bihuniak.piotr.autoconfigure.api;

import java.util.Objects;
import java.util.Set;

public class TestUser {
	public final Set<Role> roles;
	public final Set<Object> resources;

	public  TestUser(Set<Role> roles, Set<Object> resources) {
		this.roles = roles;
		this.resources = resources;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TestUser testUser = (TestUser) o;
		return Objects.equals(roles, testUser.roles) && Objects.equals(resources, testUser.resources);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roles, resources);
	}

	@Override
	public String toString() {
		return "TestUser{" +
			"roles=" + roles +
			", resources=" + resources +
			'}';
	}
}
