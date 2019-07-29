package org.fogbowcloud.app.core.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.models.OAuthToken;
import org.json.JSONObject;

import java.sql.*;
import java.util.Date;
import java.util.List;

public class OAuthTokenDataStore extends DataStore<OAuthToken> {

    private static final Logger logger = Logger.getLogger(OAuthTokenDataStore.class);

    private static final String TOKEN_VERSION = "version";
    private static final String TOKENS_TABLE_NAME = "iguassu_tokens";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String USER_ID = "user_id";
    private static final String EXPIRES_IN = "expires_in";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS "
                    + TOKENS_TABLE_NAME
                    + "("
                    + ACCESS_TOKEN
                    + " VARCHAR(255) PRIMARY KEY, "
                    + REFRESH_TOKEN
                    + " VARCHAR(255), "
                    + USER_ID
                    + " VARCHAR(255), "
                    + EXPIRES_IN
                    + " DATE,"
                    + TOKEN_VERSION
                    + " BIGINT"
                    + ")";

    private static final String INSERT_TOKEN_TABLE_SQL =
            "INSERT INTO " + TOKENS_TABLE_NAME + " VALUES(?, ?, ?, ?, ?)";

    private static final String UPDATE_TOKEN_TABLE_SQL =
            "UPDATE "
                    + TOKENS_TABLE_NAME
                    + " SET "
                    + ACCESS_TOKEN
                    + " = ?, "
                    + REFRESH_TOKEN
                    + " = ?, "
                    + USER_ID
                    + " = ?, "
                    + EXPIRES_IN
                    + " = ?, "
                    + TOKEN_VERSION
                    + " = ? WHERE "
                    + ACCESS_TOKEN
                    + " = ?";

    private static final String GET_ALL_TOKENS = "SELECT * FROM " + TOKENS_TABLE_NAME;
    private static final String GET_TOKEN_BY_ACCESS_TOKEN =
            GET_ALL_TOKENS + " WHERE " + ACCESS_TOKEN + " = ? ";
    private static final String GET_TOKEN_BY_OWNER_USER_ID =
            GET_ALL_TOKENS + " WHERE " + USER_ID + " = ? ";

    private static final String DELETE_ALL_TOKENS_TABLE_SQL = "DELETE FROM " + TOKENS_TABLE_NAME;
    private static final String DELETE_TOKEN_BY_ACCESS_TOKEN_SQL =
            DELETE_ALL_TOKENS_TABLE_SQL + " WHERE " + ACCESS_TOKEN + " = ? ";

    public OAuthTokenDataStore(String dataStoreURL) {
        super(dataStoreURL);
        Statement statement = null;
        Connection connection = null;
        try {
            logger.debug("tokenDataStoreURL: " + super.tokenDataStoreURL);

            Class.forName(DATASTORE_DRIVER);

            connection = getConnection();
            statement = connection.createStatement();
            statement.execute(CREATE_TABLE_STATEMENT);
            statement.close();
        } catch (Exception e) {
            logger.error(ERROR_WHILE_INITIALIZING_THE_DATA_STORE, e);
            throw new Error(ERROR_WHILE_INITIALIZING_THE_DATA_STORE, e);
        } finally {
            close(statement, connection);
        }
    }

    public void insert(OAuthToken token) {
        logger.debug(
                "Inserting access token ["
                        + token.getAccessToken()
                        + "] with user ["
                        + token.getUserId()
                        + "]");

        if (token.getAccessToken() == null
                || token.getAccessToken().isEmpty()
                || token.getUserId() == null
                || token.getUserId().isEmpty()) {
            logger.warn("Access token and user must not be null.");
            return;
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(INSERT_TOKEN_TABLE_SQL);
            preparedStatement.setString(1, token.getAccessToken());
            preparedStatement.setString(2, token.getRefreshToken());
            preparedStatement.setString(3, token.getUserId());
            preparedStatement.setDate(4, token.getExpirationDate());
            preparedStatement.setLong(5, token.getVersion());

            preparedStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            logger.error("Couldn't execute statement : " + INSERT_TOKEN_TABLE_SQL, e);
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                logger.error("Couldn't rollback transaction.", e1);
            }
        } finally {
            close(preparedStatement, connection);
        }
    }

    public boolean update(String oldAccessToken, OAuthToken token) {
        logger.debug(
                "Updating access token ["
                        + token.getAccessToken()
                        + "] from user ["
                        + token.getUserId()
                        + "]");

        if (token.getAccessToken() == null
                || token.getAccessToken().isEmpty()
                || token.getUserId() == null
                || token.getUserId().isEmpty()) {
            logger.warn("Access token and user must not be null.");
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
            preparedStatement.setString(3, token.getUserId());
            preparedStatement.setDate(4, token.getExpirationDate());
            preparedStatement.setLong(5, token.getVersion());
            preparedStatement.setString(6, oldAccessToken);

            preparedStatement.execute();
            connection.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Couldn't execute statement : " + UPDATE_TOKEN_TABLE_SQL, e);
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e1) {
                logger.error("Couldn't rollback transaction.", e1);
            }
            return false;
        } finally {
            close(preparedStatement, connection);
        }
    }

    public List<OAuthToken> getAll() {
        logger.debug("Getting all instances id with related orders.");
        return executeQueryStatement(GET_ALL_TOKENS);
    }

    OAuthToken getTokenByAccessToken(String accessToken) {
        List<OAuthToken> tokensList = executeQueryStatement(GET_TOKEN_BY_ACCESS_TOKEN, accessToken);
        if (tokensList != null && !tokensList.isEmpty()) {
            return tokensList.get(0);
        }
        return null;
    }

    private List<OAuthToken> getAccessTokenByUserId(String userId) {
        return executeQueryStatement(GET_TOKEN_BY_OWNER_USER_ID, userId);
    }

    void deleteAll() {
        logger.debug("Deleting all tokens.");

        Statement statement = null;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            statement = conn.createStatement();
            boolean result = statement.execute(DELETE_ALL_TOKENS_TABLE_SQL);
            conn.commit();
        } catch (SQLException e) {
            logger.error("Couldn't delete all registres on " + TOKENS_TABLE_NAME, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }
        } finally {
            close(statement, conn);
        }
    }

    public void deleteByAccessToken(String accessToken) {
        logger.debug("Deleting token with accessToken [" + accessToken + "]");

        PreparedStatement preparedStatement = null;
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement(DELETE_TOKEN_BY_ACCESS_TOKEN_SQL);
            preparedStatement.setString(1, accessToken);
            preparedStatement.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            logger.error("Couldn't delete.", e);
        } finally {
            close(preparedStatement, conn);
        }
    }

    public OAuthToken getObjFromDataStoreResult(ResultSet rs) throws SQLException {
        final String accessToken = rs.getString(ACCESS_TOKEN);
        final String refreshToken = rs.getString(REFRESH_TOKEN);
        final String userId = rs.getString(USER_ID);
        final Date expirationTime = rs.getDate(EXPIRES_IN);
        final long expirationTimeInMilliseconds = expirationTime.getTime();
        final long version = rs.getLong(TOKEN_VERSION);

        String strJson =
                "{"
                        + ACCESS_TOKEN
                        + ":"
                        + accessToken
                        + ","
                        + REFRESH_TOKEN
                        + ":"
                        + refreshToken
                        + ","
                        + USER_ID
                        + ":"
                        + userId
                        + ","
                        + EXPIRES_IN
                        + ":"
                        + expirationTimeInMilliseconds
                        + ","
                        + TOKEN_VERSION
                        + ":"
                        + version
                        + "}";
        JSONObject tokenJson = new JSONObject(strJson);
        return OAuthToken.fromJSON(tokenJson);
    }

    public OAuthToken getCurrentTokenByUserId(String userIdentification) {
        OAuthToken oAuthToken = null;
        List<OAuthToken> oAuthTokens = this.getAccessTokenByUserId(userIdentification);
        for (OAuthToken t : oAuthTokens) {
            if (oAuthToken == null || oAuthToken.getVersion() < t.getVersion()) {
                oAuthToken = t;
            }
        }
        return oAuthToken;
    }
}
