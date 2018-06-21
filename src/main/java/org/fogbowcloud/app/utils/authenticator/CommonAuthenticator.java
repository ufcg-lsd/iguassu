package org.fogbowcloud.app.utils.authenticator;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.model.UserImpl;
import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
import org.fogbowcloud.app.utils.RSAUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class CommonAuthenticator implements ArrebolAuthenticator {
	public static final Logger LOGGER = Logger.getLogger(CommonAuthenticator.class);

	public static final String AUTH_NAME = "commonauth";
	private DB usersDB;
	private ConcurrentMap<String, String> userList;
	
	public CommonAuthenticator(Properties properties) {
		final File usersFile = new File(ArrebolPropertiesConstants.DB_FILE_USERS);
		this.usersDB = DBMaker.newFileDB(usersFile).make();
		this.usersDB.checkShouldCreate(ArrebolPropertiesConstants.DB_MAP_USERS);
		this.userList = usersDB.getHashMap(ArrebolPropertiesConstants.DB_MAP_USERS);
	}
	
	public static boolean checkUserSignature(String hash, User user, int nonce)
			throws IOException, GeneralSecurityException {
		if (user == null) {
			return false;
		}
		// When using the arrebol CLI the generated base64 hash has some line
		// breaks
		// to be able to send it in the request we replace the new line char
		// with "*"
		// and here we need to replace it back with the new line char
		hash = hash.replace("*", "\n");
		RSAPublicKey publicKey = RSAUtils.getPublicKeyFromString(((UserImpl)user).getPublicKey());
		return RSAUtils.verify(publicKey, user.getUser() + nonce, hash);

	}

	@Override
	public User authenticateUser(Credential credential) {
		User user = getUserByUsername(credential.getUsername());
		String hash = credential.getPassword();
		int nonce = credential.getNonce();
		try {
			if (checkUserSignature(hash, user, nonce)) {
				return user;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getAuthenticatorName() {
		return AUTH_NAME;
	}

	@Override
	public User addUser(String username, String publicKey) {
		try {
			User user = new UserImpl(username, publicKey);
			this.userList.put(username, ((UserImpl) user).toJSON().toString());
			this.usersDB.commit();
			return user;
		} catch (Exception e) {
			throw new RuntimeException("Could not add user", e);
		}
	}
	
	@Override
	public User getUserByUsername(String username) {
		User user = null;
		String userJSONString = this.userList.get(username);
		if (userJSONString == null || userJSONString.isEmpty()) {
			return null;
		}
		try {
			user = UserImpl.fromJSON(new JSONObject(userJSONString));
		} catch (JSONException e) {
			LOGGER.debug("Could not retrieve the user from database.", e);
		}
		return user;
	}
}