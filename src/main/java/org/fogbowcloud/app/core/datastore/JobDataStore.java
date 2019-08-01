package org.fogbowcloud.app.core.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.json.JSONObject;

public class JobDataStore extends DataStore<JDFJob> {
    private static final Logger logger = Logger.getLogger(JobDataStore.class);
    private static final String ERROR_WHILE_INITIALIZING_THE_DATA_STORE =
            "Error while initializing the Job DataStore.";

    private static final String JOBS_TABLE_NAME = "iguassu_jobs";
    private static final String JOB_ID = "job_id";
    private static final String JOB_JSON = "job_json";
    private static final String JOB_USER_ID = "job_user_id";

    private static final String CREATE_TABLE_STATEMENT =
            "CREATE TABLE IF NOT EXISTS "
                    + JOBS_TABLE_NAME
                    + "("
                    + JOB_ID
                    + " VARCHAR(255) PRIMARY KEY, "
                    + JOB_USER_ID
                    + " VARCHAR(255), "
                    + JOB_JSON
                    + " TEXT)";

    private static final String INSERT_JOB_TABLE_SQL =
            "INSERT INTO " + JOBS_TABLE_NAME + " VALUES(?, ?, ?)";

    private static final String UPDATE_JOB_TABLE_SQL =
            "UPDATE "
                    + JOBS_TABLE_NAME
                    + " SET "
                    + JOB_ID
                    + " = ?, "
                    + JOB_USER_ID
                    + " = ?, "
                    + JOB_JSON
                    + " = ? WHERE "
                    + JOB_ID
                    + " = ?";

    private static final String GET_ALL_JOB = "SELECT * FROM " + JOBS_TABLE_NAME;
    private static final String GET_JOB_BY_USER_ID =
            GET_ALL_JOB + " WHERE " + JOB_USER_ID + " = ? ";
    private static final String GET_JOB_BY_JOB_ID =
            GET_ALL_JOB + " WHERE " + JOB_ID + " = ? AND " + JOB_USER_ID + " = ?";

    private static final String DELETE_ALL_JOB_TABLE_SQL = "DELETE FROM " + JOBS_TABLE_NAME;
    private static final String DELETE_BY_USER_ID =
            "DELETE FROM " + JOBS_TABLE_NAME + " WHERE " + JOB_USER_ID + " = ? ";
    private static final String DELETE_BY_JOB_ID_SQL =
            "DELETE FROM "
                    + JOBS_TABLE_NAME
                    + " WHERE "
                    + JOB_ID
                    + " = ? AND "
                    + JOB_USER_ID
                    + " = ?";

    public JobDataStore(String dataStoreURL) {
        super(dataStoreURL);
        Statement statement = null;
        Connection connection = null;

        try {
            logger.debug("jobDataStoreURL: " + super.tokenDataStoreURL);

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

    public void insert(JDFJob job) {
        logger.debug("Inserting job [" + job.getId() + "] with user id [" + job.getUserId() + "]");

        if (job.getId() == null
                || job.getId().isEmpty()
                || job.getUserId() == null
                || job.getUserId().isEmpty()) {
            logger.warn("Job Id and user id must not be null.");
            return;
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(INSERT_JOB_TABLE_SQL);
            preparedStatement.setString(1, job.getId());
            preparedStatement.setString(2, job.getUserId());
            preparedStatement.setString(3, job.toJSON().toString());

            preparedStatement.execute();
            connection.commit();
        } catch (SQLException e) {
            logger.error("Couldn't execute statement : " + INSERT_JOB_TABLE_SQL, e);
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

    public boolean update(JDFJob job) {
        logger.debug("Updating job [" + job.getId() + "] from user id [" + job.getUserId() + "]");

        if (job.getId() == null
                || job.getId().isEmpty()
                || job.getUserId() == null
                || job.getUserId().isEmpty()) {
            logger.warn("Job Id and user id must not be null.");
            return false;
        }

        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(UPDATE_JOB_TABLE_SQL);
            preparedStatement.setString(1, job.getId());
            preparedStatement.setString(2, job.getUserId());
            preparedStatement.setString(3, job.toJSON().toString());
            preparedStatement.setString(4, job.getId());

            preparedStatement.execute();
            connection.commit();
            return true;
        } catch (SQLException e) {
            logger.error("Couldn't execute statement : " + UPDATE_JOB_TABLE_SQL, e);
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

    public List<JDFJob> getAll() {
        logger.debug("Getting all instances id with related orders.");
        return executeQueryStatement(GET_ALL_JOB);
    }

    public List<JDFJob> getAllByUserId(String user) {
        logger.debug("Getting all jobs to user [" + user + "]");
        return executeQueryStatement(GET_JOB_BY_USER_ID, user);
    }

    public JDFJob getByJobId(String jobId, String userId) {
        logger.debug("Getting jobs by job ID [" + jobId + "]");

        List<JDFJob> jdfJobList = executeQueryStatement(GET_JOB_BY_JOB_ID, jobId, userId);
        if (jdfJobList != null && !jdfJobList.isEmpty()) {
            return jdfJobList.get(0);
        }
        return null;
    }

    public void deleteAll() {
        logger.debug("Deleting all jobs.");

        Statement statement = null;
        Connection conn = null;
        try {
            conn = getConnection();
            statement = conn.createStatement();

            boolean result = statement.execute(DELETE_ALL_JOB_TABLE_SQL);
            conn.commit();
        } catch (SQLException e) {
            logger.error("Couldn't delete all registers on " + JOBS_TABLE_NAME, e);
        } finally {
            close(statement, conn);
        }
    }

    public boolean deleteAllFromUser(String user) {
        logger.debug("Deleting all job from user [" + user + "].");

        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(DELETE_BY_USER_ID);
            statement.setString(1, user);
            boolean result = statement.execute();
            conn.commit();
            return result;

        } catch (SQLException e) {
            logger.error(
                    "Couldn't delete all registres from user [" + user + "] on " + JOBS_TABLE_NAME,
                    e);
            return false;
        } finally {
            close(statement, conn);
        }
    }

    public int deleteByJobId(String jobId, String user) {
        logger.debug("Deleting all jobs with id [" + jobId + "]");

        PreparedStatement statement = null;
        Connection conn = null;
        int updateCount = -1;
        try {
            conn = getConnection();
            statement = conn.prepareStatement(DELETE_BY_JOB_ID_SQL);
            statement.setString(1, jobId);
            statement.setString(2, user);

            updateCount = statement.executeUpdate();
        } catch (SQLException e) {
            logger.error(
                    "Couldn't delete registres on "
                            + JOBS_TABLE_NAME
                            + " with Job id ["
                            + jobId
                            + "]",
                    e);
        } finally {
            close(statement, conn);
        }
        return updateCount;
    }

    public JDFJob getObjFromDataStoreResult(ResultSet rs) throws SQLException {
	    return JDFJob.fromJSON(new JSONObject(rs.getString(JOB_JSON)));
    }
}
