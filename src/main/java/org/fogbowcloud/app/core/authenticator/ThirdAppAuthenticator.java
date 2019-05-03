package org.fogbowcloud.app.core.authenticator;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.authenticator.models.UserImpl;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

public class ThirdAppAuthenticator implements IguassuAuthenticator {

    private static final Logger LOGGER = Logger.getLogger(ThirdAppAuthenticator.class);

    public static final String AUTH_NAME = "third_app_auth";

    private DB usersDB;
    private ConcurrentMap<String, String> userList;

    public ThirdAppAuthenticator(Properties properties) {
        final File usersFile = new File(IguassuPropertiesConstants.DATASTORES_USERS_DB);
        this.usersDB = DBMaker.newFileDB(usersFile).make();
        this.usersDB.checkShouldCreate(IguassuPropertiesConstants.DATASTORES_USERS);
        this.userList = this.usersDB.getHashMap(IguassuPropertiesConstants.DATASTORES_USERS);
    }

    @Override
    public User authenticateUser(Credential credential) {
        User user = getUserByUsername(credential.getUsername());
        LOGGER.debug("Authenticating user with userId: " + user.getUsername());
        return user;
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
            LOGGER.info("Could not retrieve the user from database.", e);
        }
        return user;
    }

    @Override
    public String getAuthenticatorName() {
        return AUTH_NAME;
    }

}

