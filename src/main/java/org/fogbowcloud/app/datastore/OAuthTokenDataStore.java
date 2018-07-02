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
            + EXPIRATION_TIME + " DATE )";
    // TODO: check if makes sense add constraint to unique token_owner_username

    private static final String INSERT_TOKEN_TABLE_SQL = "INSERT INTO " + TOKENS_TABLE_NAME
            + " VALUES(?, ?, ?, ?)";

    private static final String UPDATE_TOKEN_TABLE_SQL = "UPDATE " + TOKENS_TABLE_NAME + " SET "
            + ACCESS_TOKEN + " = ?, " + REFRESH_TOKEN + " = ?, " + TOKEN_OWNER_USERNAME + " = ?, " + EXPIRATION_TIME
            + " = ? WHERE " + ACCESS_TOKEN + " = ?";

    private static final String GET_ALL_TOKENS = "SELECT * FROM " + TOKENS_TABLE_NAME;
    private static final String GET_TOKEN_BY_ACCESS_TOKEN = GET_ALL_TOKENS + " WHERE " + ACCESS_TOKEN + " = ? ";
    private static final String GET_TOKEN_BY_OWNER_USERNAME = GET_ALL_TOKENS + " WHERE " + TOKEN_OWNER_USERNAME + " = ? ";

    private static final String DELETE_ALL_TOKENS_TABLE_SQL = "DELETE FROM " + TOKENS_TABLE_NAME;
    private static final String DELETE_TOKEN_BY_ACCESS_TOKEN_SQL = DELETE_ALL_TOKENS_TABLE_SQL
            + " WHERE " + ACCESS_TOKEN + " + ? ";

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

    public boolean insert(OAuthToken token) {
        LOGGER.debug("Inserting access token [" + token.getAccessToken() + "] with owner [" + token.getUsernameOwner() + "]");

        if (token.getAccessToken() == null || token.getAccessToken().isEmpty()
                || token.getUsernameOwner() == null || token.getUsernameOwner().isEmpty()) {
            LOGGER.warn("Access token and owner must not be null.");
            return false;
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(INSERT_TOKEN_TABLE_SQL);
            preparedStatement.setString(1, token.getAccessToken());
            preparedStatement.setString(2, token.getRefreshToken());
            preparedStatement.setString(3, token.getUsernameOwner());
            preparedStatement.setDate (4, token.getExpirationDate());

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

    public boolean update(String oldAccessToken, OAuthToken token) {
        LOGGER.debug("Updating access token [" + token.getAccessToken() + "] from owner [" + token.getUsernameOwner() + "]");

        if (token.getAccessToken() == null || token.getAccessToken().isEmpty()
                || token.getUsernameOwner() == null || token.getUsernameOwner().isEmpty()) {
            LOGGER.warn("Access token and owner must not be null.");
            return false;
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(UPDATE_TOKEN_TABLE_SQL);
            preparedStatement.setString(1, token.getAccessToken());
            preparedStatement.setString(2, token.getRefreshToken());
            preparedStatement.setString(3, token.getUsernameOwner());
            preparedStatement.setDate(4, token.getExpirationDate());
            preparedStatement.setString(5, oldAccessToken);

            preparedStatement.execute();
            connection.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Couldn't execute statement : " + UPDATE_TOKEN_TABLE_SQL, e);
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

    public OAuthToken getTokenByAccessToken(String accessToken) {
        List<OAuthToken> tokensList = executeQueryStatement(GET_TOKEN_BY_ACCESS_TOKEN, accessToken);
        if (tokensList != null && !tokensList.isEmpty()) {
            return tokensList.get(0);
        }
        return null;
    }

    public List<OAuthToken> getAccessTokenByOwnerUsername(String ownerUsername) {
        List<OAuthToken> tokensList = executeQueryStatement(GET_TOKEN_BY_OWNER_USERNAME, ownerUsername);
        return tokensList;
    }

    public boolean deleteAll() {
        LOGGER.debug("Deleting all tokens.");

        Statement statement = null;
        Connection conn = null;
        try {
            conn = getConnection();
            statement = conn.createStatement();

            boolean result = statement.execute(DELETE_ALL_TOKENS_TABLE_SQL);
            conn.commit();
            return result;
        } catch (SQLException e) {
            LOGGER.error("Couldn't delete all registres on " + TOKENS_TABLE_NAME, e);
            return false;
        } finally {
            close(statement, conn);
        }
    }

    // TODO: test
    public boolean deleteByAccessToken(String accessToken) {
        LOGGER.debug("Deleting token with accessToken [" + accessToken + "]");

        PreparedStatement preparedStatement = null;
        Connection conn = null;
        try {
            conn = getConnection();
            preparedStatement = conn.prepareStatement(DELETE_TOKEN_BY_ACCESS_TOKEN_SQL);
            preparedStatement.setString(1, accessToken);
            ResultSet rs = preparedStatement.executeQuery();
            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.error("Couldn't delete.", e);
            return false;
        } finally {
            close(preparedStatement, conn);
        }
    }

    public OAuthToken getObjFromDataStoreResult(ResultSet rs) throws SQLException {
        String accessToken = rs.getString(ACCESS_TOKEN);
        String refreshToken = rs.getString(REFRESH_TOKEN);
        String ownerUsername = rs.getString(TOKEN_OWNER_USERNAME);
        Date expirationTime = rs.getDate(EXPIRATION_TIME);
        String strJson = "{" + ACCESS_TOKEN + ":" + accessToken + ","
                + REFRESH_TOKEN + ":" + refreshToken + ","
                + TOKEN_OWNER_USERNAME + ":" + ownerUsername + ","
                + EXPIRATION_TIME + ":" + expirationTime + "}";
        JSONObject tokenJson = new JSONObject(strJson);
        OAuthToken token = OAuthToken.fromJSON(tokenJson);
        return token;
    }

}
