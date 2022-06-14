##Security-custom-annotations-spring-boot-starter
This project is a spring boot starter, which enable to use custom annotation to secure method based on spring security 5.7.1.
To use it we have to configure a few things:

###1.Add this project to pom.xml or build.gradle:
It can be done by solution providing by 
https://jitpack.io/ </br>
In maven it can be done in this way:
```
	<repositories>
		<repository>
			<id>jitpack.io</id>
			<url>https://jitpack.io</url>
		</repository>
	</repositories>
	<dependencies>
		<dependency>
			<groupId>com.github.bihu1</groupId>
			<artifactId>security-custom-annotations-spring-boot-starter</artifactId>
			<version>1.0.0-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
```
Jitpack build this project on local machine, so spring-boot-starter-test dependency is needed for compile tests properly.
To skip running tests from this project:
```
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-surefire-plugin</artifactId>
				<version>3.0.0-M1</version>
				<configuration>
					<excludes>
						<exclude>**/SecurityCustomAnnotationsSpringBootExampleApplicationTests.java</exclude>
					</excludes>
				</configuration>
			</plugin>
		</plugins>
	</build>
```

###2.Create own custom annotation 
For example:
```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MyProjectAuthorize {
}
```
After that it is possible to add any annotation methods, which can be used to keep data
about user who can enter secured method. It can be role, capability etc. For example:
```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MyProjectAuthorize {
	Role role();
}
```
Also, there are two reserved methods, which can be used only in defined way. First one is 'ids':
```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MyProjectAuthorize {
	Role role();
	String[] ids() default {};
}
```
Adding this method to annotation enable sending of chosen method's argument to security rules. For example:
```
@MyProjectAuthorize(ids="arg1")
public void doSomething(String arg1)
```
More about that later.<br/>
Second reserved method is 'order':
```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MyProjectAuthorize {
	Role role();
	String[] ids() default {};
	String order() default "PRE";
}
```
This method enable to manage when security check have to be done. There are three options:
- PRE means before enter a method 
- POST means after enter a method
- BOTH means before and after enter a method

If this method is not added to annotation, default behaviour is BOTH.<br/>
This method returning type have to be String or Order enum, which has values like this: PRE, POST, BOTH.<br/>
It is possible to use prepared enum: pl.bihuniak.piotr.autoconfigure.api.Order

###3.Implement pl.bihuniak.piotr.autoconfigure.api.AuthorizationRuleProvider
```
public interface AuthorizationRuleProvider<T extends Annotation> {
	MainRule<T> mainRule();

	default Collection<Rule<T, ?>> preRulesForSpecificArgumentsTypes() {
		return Set.of();
	}

	default Collection<Rule<T, ?>> postRulesForSpecificReturnTypes() {
		return Set.of();
	}
}
```
This interface allows implementing three types of security rules.
Main rule is required and pre- or post- rules are optional.
```
@Component
class AuthorizationPredicatesProviderExampleImpl implements AuthorizationRuleProvider<MyProjectAuthorize> {

	@Override
	public MainRule<MyProjectAuthorize> mainRule() {
		return new MainRule<>(
			(Authentication auth, MyProjectAuthorize ann) ->
				((ExampleUserProvider) auth.getPrincipal()).getCurrent().roles.contains(ann.role())
		);
	}
}
```
Main rule is invoke always when none of pre- or post- rule are declared.
If app use only pre- or post- rules there is highly recommend to use main rule like this:
```
	@Override
	public MainRule<MyProjectAuthorize> mainRule() {
		return new MainRule<>(
			(Authentication auth, MyProjectAuthorize ann) -> false
		);
	}
```
In that if for some case pre- or -post rule is accidentally not declared, there will be no security gap.<br/>
Pre- or post- rule can be declared like this:
```
	@Override
	public Collection<Rule<MyProjectAuthorize, ?>> preRulesForSpecificArgumentsTypes() {
		return Set.of(
				new Rule<>(Id.class,
					(Authentication auth, MyProjectAuthorize ann, Id id) -> {
						ExampleUser current = ((ExampleUserProvider) auth.getPrincipal()).getCurrent();
						return current.roles.contains(ann.role()) && current.resources.contains(id);
					}
				)
			);
	}

	@Override
	public Collection<Rule<MyProjectAuthorize, ?>> postRulesForSpecificReturnTypes() {
		return Set.of(
			new Rule<>(Role.class,
				(Authentication auth, MyProjectAuthorize ann, Role role) -> {
					ExampleUser current = ((ExampleUserProvider) auth.getPrincipal()).getCurrent();
					return current.roles.contains(ann.role()) && current.roles.contains(role);
				}
			));
	}
```
Pre- or post- rule are based on method types.</br>
Pre- on the argument type.</br>
Post- on the returning type.</br>
So rule have to be unique for each type, using for security.
Based on the previous examples, next one, for this method:
```
@MyProjectAuthorize(ids="id", order="PRE")
public void doSomething(Id id)
```
This rule will be invoked:
```
new Rule<>(Id.class,
	(Authentication auth, MyProjectAuthorize ann, Id id) -> {
		ExampleUser current = ((ExampleUserProvider) auth.getPrincipal()).getCurrent();
		return current.roles.contains(ann.role()) && current.resources.contains(id);
	}
)
```
###4.Create UserProvider
Last step to take is modifying org.springframework.security.core.Authentication.getPrincipal()
method in order that return custom object. For each type of authorizations it has to be done in different way.
All needed information can be found in spring security documentation, in this documentation it will be shown for oAuth2 and basicAuth.</br>
First it is needed to create own interface, which will be user provider. For example:
```
interface ExampleUserProvider {
	ExampleUser getCurrent();
}
```
For basicAuth:
```
class ExampleUserPrincipal extends org.springframework.security.core.userdetails.User implements ExampleUserProvider {

	private final ExampleUser appUser;

	public ExampleUserPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities,
	                            ExampleUser appUser) {
		super(username, password, authorities);
		this.appUser = appUser;
	}

	@Override
	public ExampleUser getCurrent() {
		return appUser;
	}
}
```

```
@Service
class UserService implements UserDetailsService {
	private final Map<String, ExampleUserPrincipal> usernameToPassword;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		ExampleUserPrincipal principal = usernameToPassword.get(username);
		if(principal == null)
			throw new UsernameNotFoundException("USER NOT FOUND");
		return principal;
	}
}
```

```
@Bean
public DaoAuthenticationProvider authenticationProvider(BCryptPasswordEncoder passwordEncoder, UserService userService){
	DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	provider.setPasswordEncoder(passwordEncoder);
	provider.setUserDetailsService(userService);
	return provider;
}
```

For oAuth2:
```
public class ExampleUserPrincipal extends org.springframework.security.oauth2.core.user.DefaultOAuth2User implements ExampleUserProvider {
	public ExampleUser user;

	FurmsOAuthAuthenticatedUser(OAuth2User defaultOAuth2User, String key, ExampleUser user) {
		super(defaultOAuth2User.getAuthorities(), defaultOAuth2User.getAttributes(), key);
		this.user = user;
	}

	@Override
	public ExampleUser getCurrent() {
		return furmsUser;
	}
}
```

```
public class ExampleOAuth2UserService extends org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService {
	private final Map<String, ExampleUserPrincipal> subToUser;

	public ExampleOAuth2UserService() {
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String key = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
			.getUserNameAttributeName();
		String sub = oAuth2User.getAttribute("sub");
		try {
			ExampleUserPrincipal user = subToUser.get(sub);
			return new ExampleOAuthAuthenticatedUser(oAuth2User, key, user);
		}catch (RoleLoadingException e){
			throw new OAuth2AuthenticationException(new OAuth2Error(e.code), e);
		}
	}
```

```
http
    ...
    .and().oauth2Login()
    .userInfoEndpoint().userService(new ExampleOAuth2UserService())
```

####To see example: 
https://github.com/bihu1/security-custom-annotations-spring-boot-example.



