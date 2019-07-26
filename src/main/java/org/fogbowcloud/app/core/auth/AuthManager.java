package org.fogbowcloud.app.core.auth;

import java.security.GeneralSecurityException;
import org.fogbowcloud.app.core.auth.models.Credential;
import org.fogbowcloud.app.core.auth.models.User;

/**
 * This interface defines the operations that an authentication and authorization manager must have.
 */
public interface AuthManager {

	/**
	 * Retrieves an authorized user. This operation verifies if the user already has some registry of
	 * authentication and if he has an updated and the valid of it credentials.
	 *
	 * @param credentials of the user.
	 * @return an authorized user instance.
	 * @throws GeneralSecurityException if any information has a wrong shape or is expired.
	 */
	User authorize(Credential credentials) throws GeneralSecurityException;

	/**
	 * Stores a new user on the database generating an secret token.
	 *
	 * @param userId is the identifier of the user. Must be unique.
	 * @param secretToken
	 * @return
	 */
	User store(String userId, String secretToken);

	/**
	 * Returns the user if they have already authenticated before. May be null depending on the if the
	 * {@link User doesn't exist}.
	 */
	User retrieve(String userId);

	/** Replaces the current user state by the user state received in params. */
	void update(User currentUser);
}
