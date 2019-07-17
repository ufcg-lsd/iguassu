package org.fogbowcloud.app.core.authenticator;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.authenticator.models.UserImpl;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class CommonAuthenticator implements IguassuAuthenticator {

    private static final Logger LOGGER = Logger.getLogger(CommonAuthenticator.class);

    private DB usersDB;
    private ConcurrentMap<String, String> userList;

    public CommonAuthenticator() {
        final File usersFile = new File(IguassuPropertiesConstants.DATASTORES_USERS_DB_FILE_PATH);
        this.usersDB = DBMaker.newFileDB(usersFile).make();
        this.usersDB.checkShouldCreate(IguassuPropertiesConstants.DATASTORES_USERS_FILE_PATH);
        this.userList = this.usersDB
            .getHashMap(IguassuPropertiesConstants.DATASTORES_USERS_FILE_PATH);
    }

    @Override
    public User authorizesUser(Credential credential) throws GeneralSecurityException {
        User user = Objects.requireNonNull(getUserByUsername(credential.getUserId()));
        LOGGER.debug("Authorizing user with userId: " + user.getUserIdentification());

        if (!user.getIguassuToken().equalsIgnoreCase(credential.getIguassuToken())) {
            throw new GeneralSecurityException(
                "User " + user.getUserIdentification() + " not has a valid Iguassu token.");
        }

        return user;
    }

    @Override
    public User addUser(String username, String iguassuToken) {
        try {
            UserImpl user = new UserImpl(username, iguassuToken);
            this.userList.put(username, user.toJSON().toString());
            this.usersDB.commit();
            LOGGER.info("User with userId " + username + " added.");
            return user;
        } catch (Exception e) {
            throw new RuntimeException("Could not add user", e);
        }
    }

    @Override
    public void updateUser(User currentUser) {
        User outdatedUser = UserImpl
            .fromJSON(new JSONObject(this.userList.get(currentUser.getUserIdentification())));

        if (!outdatedUser.equals(currentUser)) {
            LOGGER.debug(
                "Updating user: " + this.userList.remove(currentUser.getUserIdentification()));
            this.userList
                .put(currentUser.getUserIdentification(), UserImpl.toJSON(currentUser).toString());
            this.usersDB.commit();
            LOGGER.debug("User updated: " + this.userList.get(currentUser.getUserIdentification()));
        } else {
            LOGGER.debug("User has no changes.");
        }

    }

    @Override
    public User getUserByUsername(String userId) {
        User user = null;
        String userJSONString = this.userList.get(userId);

        if (userJSONString == null || userJSONString.isEmpty()) {
            LOGGER.info("There is no user with username " + userId);
            return null;
        }
        try {
            user = UserImpl.fromJSON(new JSONObject(userJSONString));
        } catch (JSONException e) {
            LOGGER.info("Could not retrieve the user from database.", e);
        }
        return user;
    }


}

