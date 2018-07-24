package org.fogbowcloud.app.datastore;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.model.OAuthToken;
import org.fogbowcloud.app.utils.DataStoreHelper;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public abstract class DataStore<T> {

    protected static final String JOB_DATASTORE_DRIVER = "org.sqlite.JDBC";
    protected static final String ERROR_WHILE_INITIALIZING_THE_DATA_STORE = "Error while initializing the Job DataStore.";
    protected static final String DEFAULT_DATASTORE_NAME = "datastore.slite";

    String dataStoreURL;

    private static final Logger LOGGER = Logger.getLogger(DataStore.class);

    public DataStore(String dataStoreURL) {
        this.dataStoreURL = DataStoreHelper.getDataStoreUrl(dataStoreURL, DEFAULT_DATASTORE_NAME);
    }

    /**
     * @return the connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(this.dataStoreURL);
        } catch (SQLException e) {
            LOGGER.error("Error while getting a new connection from the connection pool.", e);
            throw e;
        }
    }

    protected void close(Statement statement, Connection conn) {
        if (statement != null) {
            try {
                if (!statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                LOGGER.error("Couldn't close statement");
            }
        }

        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                LOGGER.error("Couldn't close connection");
            }
        }
    }

    abstract T getObjFromDataStoreResult(ResultSet resultSet) throws SQLException;

    protected List<T> executeQueryStatement(String queryStatement, String... params) {
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
                    LOGGER.error("Error while mounting token from DB.", e);
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Couldn't get data from DB.", e);
            return new ArrayList<>();
        } finally {
            close(preparedStatement, conn);
        }
        LOGGER.debug("There are " + dataList.size() + " rows at DB to this query (" + preparedStatement.toString() + ").");
        return dataList;
    }

}
