package org.fogbowcloud.app.core.auth;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.models.Credential;
import org.fogbowcloud.app.core.auth.models.DefaultUser;
import org.fogbowcloud.app.core.auth.models.User;
import org.fogbowcloud.app.core.constants.ConfProperties;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

public class DefaultAuthManager implements AuthManager {

    private static final Logger logger = Logger.getLogger(DefaultAuthManager.class);

    private DB usersDB;
    private ConcurrentMap<String, String> userList;

    public DefaultAuthManager() {
        final File usersFile = new File(ConfProperties.DATASTORES_USERS_DB_FILE_PATH);
        this.usersDB = DBMaker.newFileDB(usersFile).make();
        this.usersDB.checkShouldCreate(ConfProperties.DATASTORES_USERS_FILE_PATH);
        this.userList = this.usersDB.getHashMap(ConfProperties.DATASTORES_USERS_FILE_PATH);
    }

    @Override
    public User authorize(Credential credentials) throws GeneralSecurityException {
        User user;
        try {
            user = Objects.requireNonNull(retrieve(credentials.getUserId()));
        } catch (NullPointerException npe) {
            throw new GeneralSecurityException("The user couldn't be retrieved.");
        }

        logger.debug("Authorizing user with identifier: " + user.getIdentifier());

        if (!user.retrieveToken().equalsIgnoreCase(credentials.getIguassuToken())) {
            throw new GeneralSecurityException(
                    "User " + user.getIdentifier() + " not has a valid Iguassu token.");
        }

        return user;
    }

    @Override
    public User store(String username, String token) {
        try {
            DefaultUser user = new DefaultUser(username, token);
            this.userList.put(username, user.toJSON().toString());
            this.usersDB.commit();
            logger.info("User with userId " + username + " stored.");
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Could not store user, caused by error: ", e);
        }
    }

    @Override
    public void update(User currentUser) {
        final User outdatedUser =
                DefaultUser.fromJSON(
                        new JSONObject(this.userList.get(currentUser.getIdentifier())));

        if (!outdatedUser.equals(currentUser)) {
            logger.debug("Updating user: " + this.userList.remove(currentUser.getIdentifier()));
            this.userList.put(
                    currentUser.getIdentifier(), DefaultUser.toJSON(currentUser).toString());
            this.usersDB.commit();
            logger.debug("User updated: " + this.userList.get(currentUser.getIdentifier()));
        } else {
            logger.debug("User has no changes.");
        }
    }

    @Override
    public User retrieve(String userId) {
        User user = null;
        String userJSONString = this.userList.get(userId);

        if (userJSONString == null || userJSONString.isEmpty()) {
            logger.info("There is no user with username " + userId);
            return null;
        }
        try {
            user = DefaultUser.fromJSON(new JSONObject(userJSONString));
        } catch (JSONException e) {
            logger.info("Could not retrieve the user from database.", e);
        }
        return user;
    }
}
