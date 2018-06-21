package org.fogbowcloud.app.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.OAuthToken;
import org.fogbowcloud.app.utils.DataStoreHelper;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;


public class OAuthTokenDataStore extends DataStore<OAuthToken> {

    private static final String TOKENS_TABLE_NAME = "iguassu_tokens";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String TOKEN_OWNER_USERNAME = "token_owner_username";
    private static final String EXPIRATION_TIME = "expiration_date";

    private static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS " + TOKENS_TABLE_NAME + "("
            + ACCESS_TOKEN + " VARCHAR(255) PRIMARY KEY, "
            + REFRESH_TOKEN + " VARCHAR(255), "
            + TOKEN_OWNER_USERNAME + " VARCHAR(255), "
            + EXPIRATION_TIME + " DATE, "
            + "UNIQUE (" + TOKEN_OWNER_USERNAME + "))";

    private static final String INSERT_TOKEN_TABLE_SQL = "INSERT INTO " + TOKENS_TABLE_NAME
            + " VALUES(?, ?, ?, ?)";

    private static final String GET_ALL_TOKENS = "SELECT * FROM " + TOKENS_TABLE_NAME;

    private static final Logger LOGGER = Logger.getLogger(OAuthTokenDataStore.class);

    public OAuthTokenDataStore(String dataStoreURL) {
        super(dataStoreURL);

        Statement statement = null;
        Connection connection = null;
        try {
            LOGGER.debug("tokenDataStoreURL: " + super.dataStoreURL);

            Class.forName(JOB_DATASTORE_DRIVER);

            connection = getConnection();
            statement = connection.createStatement();
            statement.execute(CREATE_TABLE_STATEMENT);
            statement.close();
        } catch (Exception e) {
            LOGGER.error(ERROR_WHILE_INITIALIZING_THE_DATA_STORE, e);
            throw new Error(ERROR_WHILE_INITIALIZING_THE_DATA_STORE, e);
        } finally {
            close(statement, connection);
        }
    }

    public boolean insert(OAuthToken accessToken) {
        LOGGER.debug("Inserting access token [" + accessToken.getAccessToken() + "] with owner [" + accessToken.getUsernameOwner() + "]");

//        if (job.getId() == null || job.getId().isEmpty()
//                || job.getOwner() == null || job.getOwner().isEmpty()) {
//            LOGGER.warn("Job Id and owner must not be null.");
//            return false;
//        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(INSERT_TOKEN_TABLE_SQL);
            preparedStatement.setString(1, accessToken.getAccessToken());
            preparedStatement.setString(2, accessToken.getRefreshToken());
            preparedStatement.setString(3, accessToken.getUsernameOwner());
            preparedStatement.setDate (4, accessToken.getExpirationDate());

            preparedStatement.execute();
            connection.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Couldn't execute statement : " + INSERT_TOKEN_TABLE_SQL, e);
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                LOGGER.error("Couldn't rollback transaction.", e1);
            }
            return false;
        } finally {
            close(preparedStatement, connection);
        }
    }

    public List<OAuthToken> getAll() {
        LOGGER.debug("Getting all instances id with related orders.");
        return executeQueryStatement(GET_ALL_TOKENS);
    }

    public OAuthToken getObjFromDataStoreResult(ResultSet rs) throws SQLException {
        String accessToken = rs.getString(ACCESS_TOKEN);
        String refreshToken = rs.getString(REFRESH_TOKEN);
        String ownerUsername = rs.getString(TOKEN_OWNER_USERNAME);
        Date expirationTime = rs.getDate(EXPIRATION_TIME);
        String strJson = "{" + ACCESS_TOKEN + ":" + accessToken + ","
                + REFRESH_TOKEN + ":" + refreshToken + ","
                + TOKENS_TABLE_NAME + ":" + ownerUsername + ","
                + EXPIRATION_TIME + ":" + expirationTime + "}";
        JSONObject tokenJson = new JSONObject(strJson);
        OAuthToken token = OAuthToken.fromJSON(tokenJson);
        return token;
    }

}