/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.util.mordred;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;


/**
 * <p>
 * This is a <b>reliable</b> DataSource implementation, based on the pooling logic written for <a
 * href="http://share.whichever.com/">Town</a> and the configuration found in Avalon's excalibur
 * code.
 * </p>
 *
 * <p>
 * This uses the normal <code>java.sql.Connection</code> object and
 * <code>java.sql.DriverManager</code>.  The Configuration is like this:
 * <pre>
 *   &lt;jdbc&gt;
 *     &lt;pool-controller min="<i>5</i>" max="<i>10</i>" connection-class="<i>my.overrided.ConnectionClass</i>"&gt;
 *       &lt;keep-alive&gt;select 1&lt;/keep-alive&gt;
 *     &lt;/pool-controller&gt;
 *     &lt;driver&gt;<i>com.database.jdbc.JdbcDriver</i>&lt;/driver&gt;
 *     &lt;dburl&gt;<i>jdbc:driver://host/mydb</i>&lt;/dburl&gt;
 *     &lt;user&gt;<i>username</i>&lt;/user&gt;
 *     &lt;password&gt;<i>password</i>&lt;/password&gt;
 *   &lt;/jdbc&gt;
 * </pre>
 * </p>
 *
 * @version CVS $Revision: 494012 $
 * @since 4.0
 */
public class JdbcDataSource extends AbstractLogEnabled
     {
    // The limit that an active connection can be running
    public static final long ACTIVE_CONN_TIME_LIMIT = 60000; // (one minute)
    public static final long ACTIVE_CONN_HARD_TIME_LIMIT = 2*ACTIVE_CONN_TIME_LIMIT;
    // How long before you kill off a connection due to inactivity
    public static final long CONN_IDLE_LIMIT        = 600000; // (10 minutes)
    private static final boolean DEEP_DEBUG         = false;
    private static int total_served                 = 0;
    // This is a temporary variable used to track how many active threads
    // are in createConnection().  This is to prevent to many connections
    // from being opened at once.
    private int connCreationsInProgress             = 0;
    // The error message is the conn pooler cannot serve connections for whatever reason
    private String connErrorMessage                 = null;
    // the last time a connection was created...
    private long connLastCreated                    = 0;
    // connection number for like of this broker
    private int connectionCount;
    // Driver class
    private String jdbcDriver;
    // Password to login to database
    private String jdbcPassword;
    // Server to connect to database (this really is the jdbc URL)
    private String jdbcURL;
    // Username to login to database
    private String jdbcUsername;
    // Maximum number of connections to have open at any point
    private int maxConn                             = 0;
    // collection of connection objects
    private ArrayList pool;
    // Thread that checks for dead/aged connections and removes them from pool
    private Thread reaper;
    // Flag to indicate whether reaper thread should run
    private boolean reaperActive                    = false;
    // a SQL command to execute to see if the connection is still ok
    private String verifyConnSQL;

    /**
     * Implements the ConnDefinition behavior when a connection is needed. Checks the pool of
     * connections to see if there is one available.  If there is not and we are below the max
     * number of connections limit, it tries to create another connection.  It retries this 10
     * times until a connection is available or can be created
     *
     * @return java.sql.Connection
     * @throws SQLException Document throws!
     */
    public Connection getConnection() throws SQLException {
       return SQLBase.getConnection();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(final Configuration configuration)
                   throws ConfigurationException {

    }

    /**
     * The dispose operation is called at the end of a components lifecycle.
     * Cleans up all JDBC connections.
     *
     * @throws Exception if an error is encountered during shutdown
     */
    public void dispose() {
        // Stop the background monitoring thread
        if(reaper != null) {
            reaperActive = false;
            //In case it's sleeping, help it quit faster
            reaper.interrupt();
            reaper = null;
        }
        // The various entries will finalize themselves once the reference
        // is removed, so no need to do it here
    }




    protected void debug(String message) {
        getLogger().debug(message);
    }

    protected void info(String message) {
        getLogger().info(message);
    }

    /*
     * This is a real hack, but oh well for now
     */
    protected void warn(String message) {
        getLogger().warn(message);
    }



}
