package org.fogbowcloud.app.utils.authenticator;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.external.oauth.ExternalOAuthController;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.model.UserImpl;
import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

public class ThirdAppAuthenticator implements IguassuAuthenticator {

    public static final Logger LOGGER = Logger.getLogger(ThirdAppAuthenticator.class);

    public static final String AUTH_NAME = "third_app_auth";

    private DB usersDB;
    private ConcurrentMap<String, String> userList;
    private ExternalOAuthController externalOAuthTokenController;

    public ThirdAppAuthenticator(Properties properties) {
        final File usersFile = new File(ArrebolPropertiesConstants.DB_FILE_USERS);
        this.usersDB = DBMaker.newFileDB(usersFile).make();
        this.usersDB.checkShouldCreate(ArrebolPropertiesConstants.DB_MAP_USERS);
        this.userList = this.usersDB.getHashMap(ArrebolPropertiesConstants.DB_MAP_USERS);
        this.externalOAuthTokenController = new ExternalOAuthController(properties);
    }

    @Override
    public User authenticateUser(Credential credential) {
        User user = getUserByUsername(credential.getUsername());
        try {
            boolean userHasAccountInFileDriver = this.externalOAuthTokenController.userExists(credential.getUsername());
            if (userHasAccountInFileDriver) {
                return user;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public User addUser(String username, String token) {
        try {
            User user = new UserImpl(username, token);
            this.userList.put(username, ((UserImpl) user).toJSON().toString());
            this.usersDB.commit();
            LOGGER.info("User with username " + username + " added.");
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
            LOGGER.info("There is no user with username " + username);
            return null;
        }
        try {
            user = UserImpl.fromJSON(new JSONObject(userJSONString));
        } catch (JSONException e) {
            LOGGER.debug("Could not retrieve the user from database.", e);
        }
        return user;
    }

    @Override
    public String getAuthenticatorName() {
        return AUTH_NAME;
    }

}

