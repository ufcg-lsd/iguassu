package org.fogbowcloud.app.integration;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.model.User;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;

public class FakeTokenUpdatePlugin extends AbstractTokenUpdatePlugin {

	private static final long EXPIRATION_INTERVAL = TimeUnit.DAYS.toMillis(365); // One year
	
	public FakeTokenUpdatePlugin(Properties properties) {
		super(properties);
	}

	@Override
	public Token generateToken() {
		Date expirationDate = new Date(new Date().getTime() + EXPIRATION_INTERVAL);
		Map<String, String> attributes = new HashMap<String, String>();
		return new Token("fakeTokenAccess", new User(UUID.randomUUID().toString(),
				"fakeUsername",
				"arrebolservice") );
	}

	@Override
	public void validateProperties() throws BlowoutException {
		// TODO Auto-generated method stub

	}

}
