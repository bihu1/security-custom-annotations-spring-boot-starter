/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package pl.bihuniak.piotr.autoconfigure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.bihuniak.piotr.autoconfigure.api.Id;
import pl.bihuniak.piotr.autoconfigure.api.Id2;
import pl.bihuniak.piotr.autoconfigure.api.Id3;
import pl.bihuniak.piotr.autoconfigure.api.Role;
import pl.bihuniak.piotr.autoconfigure.api.TestUser;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class AuthTest {

	@Autowired
	private ServiceMock mockService;

	@MockBean
	AuthenticationManager authenticationManager;

	@Mock
	Authentication authentication;

	@Mock
	UserProvider provider;

	@BeforeEach
	void configSecurityContext(){
		when(authentication.getPrincipal()).thenReturn(provider);
		when(authentication.isAuthenticated()).thenReturn(true);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	void authShouldPassWhenMainRuleIsValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of()));

		Throwable throwable = catchThrowable(() -> mockService.checkMainRule());
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldNotPassWhenMainClassRuleIsNotValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of()));

		assertThrows(AccessDeniedException.class, () -> mockService.checkClassRule());
	}

	@Test
	void authShouldPassWhenMainClassRuleIsValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of()));

		Throwable throwable = catchThrowable(() -> mockService.checkClassRule());
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldNotPassWhenMainRuleIsNotValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of()));

		assertThrows(AccessDeniedException.class, () -> mockService.checkMainRule());
	}

	@Test
	void authShouldPassWhenSinglePreRuleIsValid() {
		Id id = new Id("1");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of(id)));

		Throwable throwable = catchThrowable(() -> mockService.checkPreSingleRule(id));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldNotPassWhenSinglePreRuleIsNotValid() {
		Id id = new Id("1");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of(id)));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPreSingleRule(new Id("2")));

	}

	@Test
	void authShouldPassWhenDoublePreRulesAreValid() {
		Id id = new Id("1");
		Id2 id2 = new Id2("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of(id, id2)));

		Throwable throwable = catchThrowable(() -> mockService.checkPreDoubleRules(id, id2));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldNotPassWhenOnlyOneRuleIsValid() {
		Id id = new Id("1");
		Id2 id2 = new Id2("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of(id)));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPreDoubleRules(id, id2));
	}

	@Test
	void authShouldRunMainRuleWhenNotSupportedTypeIsArgumentValid() {
		Id3 id3 = new Id3("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of()));

		Throwable throwable = catchThrowable(() -> mockService.checkPreSingleNotSupportedType(id3));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldRunMainRuleWhenNotSupportedTypeIsArgumentNotValid() {
		Id3 id3 = new Id3("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of()));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPreSingleNotSupportedType(id3));
	}

	@Test
	void authShouldRunMainRuleWhenNotSupportedTypeIsOneOfArgumentsValid() {
		Id id = new Id("1");
		Id3 id3 = new Id3("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of(id)));

		Throwable throwable = catchThrowable(() -> mockService.checkPreSingleHalfNotSupportedType(id, id3));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldRunMainRuleWhenNotSupportedTypeIsOneOfArgumentsNotValid() {
		Id id = new Id("1");
		Id3 id3 = new Id3("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of(id)));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPreSingleHalfNotSupportedType(id, id3));
	}

	@Test
	void authShouldNotPassWhenPrePostRuleIsNotValid() {
		Id id = new Id("1");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of(id)));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPrePostRule(id));
	}


	@Test
	void authShouldPassWhenPrePostRuleIsValid() {
		Id id = new Id("1");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of(id)));

		Throwable throwable = catchThrowable(() -> mockService.checkPrePostRule(id));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldRunMainPreRuleWhenNotSupportedTypeIsReturnedValid() {
		Id id = new Id("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of(id)));

		Throwable throwable = catchThrowable(() -> mockService.checkPreRuleAndPostRuleNotSupportedType(id));
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldRunMainPreRuleWhenNotSupportedTypeIsReturnedNotValid() {
		Id id = new Id("2");
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of(id)));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPreRuleAndPostRuleNotSupportedType(id));
	}

	@Test
	void authShouldNotPassWhenPostRuleIsNotValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of()));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPostRule());
	}


	@Test
	void authShouldPassWhenPostRuleIsValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of()));

		Throwable throwable = catchThrowable(() -> mockService.checkPostRule());
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldRunMainRuleWhenNotSupportedTypeIsReturnedValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.USER), Set.of()));

		Throwable throwable = catchThrowable(() -> mockService.checkPostRuleNotSupportedType());
		assertThat(throwable).isNull();
	}

	@Test
	void authShouldRunMainRuleWhenNotSupportedTypeIsReturnedNotValid() {
		when(provider.getCurrent()).thenReturn(new TestUser(Set.of(Role.ADMIN), Set.of()));

		assertThrows(AccessDeniedException.class, () -> mockService.checkPostRuleNotSupportedType());
	}
}