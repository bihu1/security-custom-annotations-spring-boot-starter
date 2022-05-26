package pl.bihuniak.piotr.autoconfigure;

import pl.bihuniak.piotr.autoconfigure.api.TestUser;

interface UserProvider {
	TestUser getCurrent();
}
