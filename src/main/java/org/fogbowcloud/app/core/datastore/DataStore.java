package org.fogbowcloud.app.core.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.utils.DataStoreUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for the abstraction of database management.
 *
 * @param <T> is the type of object that will be manipulated.
 */
abstract class DataStore<T> {

    static final String DATASTORE_DRIVER = "org.sqlite.JDBC";
    static final String ERROR_WHILE_INITIALIZING_THE_DATA_STORE =
            "Error while initializing the Job DataStore.";
    private static final Logger logger = Logger.getLogger(DataStore.class);
    private static final String DEFAULT_DATASTORE_NAME = "datastore.sqlite";

    String tokenDataStoreURL;

    DataStore(String tokenDataStoreURL) {
        this.tokenDataStoreURL =
                DataStoreUtils.getDataStoreUrl(tokenDataStoreURL, DEFAULT_DATASTORE_NAME);
    }

    /**
     * @return the connection
     * @throws SQLException if some sql operation failed for communication problems.
     */
    Connection getConnection() throws SQLException {

        try {
            return DriverManager.getConnection(this.tokenDataStoreURL);
        } catch (SQLException e) {
            logger.error("Error while getting a new connection from the connection pool.", e);
            throw e;
        }
    }

    void close(Statement statement, Connection conn) {
        if (statement != null) {
            try {
                if (!statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                logger.error("Couldn't close statement");
            }
        }

        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Couldn't close connection");
            }
        }
    }

    abstract T getObjFromDataStoreResult(ResultSet resultSet) throws SQLException;

    List<T> executeQueryStatement(String queryStatement, String... params) {
        PreparedStatement preparedStatement = null;
        Connection conn = null;
        List<T> dataList = new ArrayList<>();

        try {
            conn = getConnection();
            preparedStatement = conn.prepareStatement(queryStatement);

            if (params != null && params.length > 0) {
                for (int index = 0; index < params.length; index++) {
                    preparedStatement.setString(index + 1, params[index]);
                }
            }

            ResultSet rs = preparedStatement.executeQuery();

            if (rs != null) {
                try {
                    while (rs.next()) {
                        T data = getObjFromDataStoreResult(rs);
                        dataList.add(data);
                    }
                } catch (Exception e) {
                    logger.error("Error while mounting token from DB.", e);
                }
            }

        } catch (SQLException e) {
            logger.error("Couldn't get data from DB.", e);
            return new ArrayList<>();
        } finally {
            close(preparedStatement, conn);
        }
        logger.debug(
                "There are "
                        + dataList.size()
                        + " rows at DB to this query ("
                        + preparedStatement.toString()
                        + ").");
        return dataList;
    }
}
