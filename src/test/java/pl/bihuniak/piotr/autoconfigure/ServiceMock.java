/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package pl.bihuniak.piotr.autoconfigure;

import org.springframework.stereotype.Service;
import pl.bihuniak.piotr.autoconfigure.api.Id;
import pl.bihuniak.piotr.autoconfigure.api.Id2;
import pl.bihuniak.piotr.autoconfigure.api.Id3;
import pl.bihuniak.piotr.autoconfigure.api.Role;
import pl.bihuniak.piotr.autoconfigure.api.TestAuthorize;

@Service
@TestAuthorize(role = Role.USER)
public class ServiceMock {

	@TestAuthorize(role = Role.ADMIN)
	public void checkMainRule() {
	}

	@TestAuthorize(role = Role.ADMIN, ids = "id")
	public void checkPreSingleRule(Id id) {
	}

	@TestAuthorize(role = Role.ADMIN, ids = {"id", "id2"})
	public void checkPreDoubleRules(Id id, Id2 id2) {
	}

	@TestAuthorize(role = Role.ADMIN, ids = "id")
	public void checkPreSingleNotSupportedType(Id3 id) {
	}

	@TestAuthorize(role = Role.ADMIN, ids = {"id", "id3"})
	public void checkPreSingleHalfNotSupportedType(Id id, Id3 id3) {
	}

	@TestAuthorize(role = Role.ADMIN, ids = "id")
	public Role checkPrePostRule(Id id) {
		return Role.ADMIN;
	}

	@TestAuthorize(role = Role.USER, ids = "id")
	public String checkPreRuleAndPostRuleNotSupportedType(Id id) {
		return Role.ADMIN.name();
	}

	@TestAuthorize(role = Role.ADMIN)
	public Role checkPostRule() {
		return Role.ADMIN;
	}

	@TestAuthorize(role = Role.USER)
	public String checkPostRuleNotSupportedType() {
		return Role.ADMIN.name();
	}

	public void checkClassRule() {
	}
}
