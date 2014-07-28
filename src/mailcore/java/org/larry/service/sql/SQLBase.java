package org.larry.service.sql;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.larry.config.Configuration;


import java.sql.*;

/**
 * User: Larry
 * Date: 3/11/13
 * Time: 7:35 PM
 */
public class SQLBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLBase.class);
    private static final int PRE_ALLOCATE_CONNECTION = 8;
    private static final ComboPooledDataSource cpds;
    private static final String identifierQuoteString;

    static {
        try {
            cpds = new ComboPooledDataSource();
            cpds.setDriverClass("com.mysql.jdbc.Driver");
            cpds.setJdbcUrl("jdbc:mysql://" + Configuration.getMysqlServerIp() +":" +
                    Configuration.getMysqlServerPort() + "/" +
                    Configuration.getMysqlDatabase() +
                    "? autoReconnect=true & failOverReadOnly=false & maxReconnects=2 & initialTimeout= 2 &  connectTimeout=2000 & socketTimeout=2000& useLocalSessionState=true & paranoid=true");
            cpds.setUser(Configuration.getMysqlServerUser());
            cpds.setPassword(Configuration.getMysqlServerPassword());
            cpds.setMaxPoolSize(32);
            cpds.setMaxConnectionAge(6 * 60 * 60); //6h
            cpds.setAutoCommitOnClose(true);
            cpds.setIdleConnectionTestPeriod(30);
            identifierQuoteString = cpds.getConnection().getMetaData().getIdentifierQuoteString();

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * getConnection hang fix: preallocate connections
     */
    private static Thread connectionPoolInitializer = new Thread(new Runnable() {
        public void run() {
            try {
                for (int i = 0; i < PRE_ALLOCATE_CONNECTION; i++) {
                    Connection con = getConnection();
                    con.close();
                    System.out.println("initialized connection: " + i);
                }

            } catch (SQLException e) {
                LOGGER.warn("init get connection failed");
            }
        }
    });

    static {
        connectionPoolInitializer.start();
    }

    public static final Connection getConnection() throws SQLException {
        Connection res = cpds.getConnection();
        res.setAutoCommit(true);
        return res;
    }

    private static final ThreadLocal<Boolean> currentlyInTransaction = new ThreadLocal<Boolean>();

    static {
        currentlyInTransaction.set(false);
    }

    private static void tryCommit(Connection connection) {
        try {
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.warn("Failed to commit, database may be in unreliable state" + e.getMessage());
        }
    }

    private static void tryRollback(Connection connection) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            LOGGER.warn("Failed to commit, database may be in unreliable state" + e.getMessage());
        }
    }

    public static boolean doesTableExist(Connection connection, String table) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, null, table, null);
        try {
            return resultSet.next();
        } finally {
            resultSet.close();
        }
    }

    public static String quoteIdentifier(String s) {
        return identifierQuoteString + s + identifierQuoteString;
    }


}
