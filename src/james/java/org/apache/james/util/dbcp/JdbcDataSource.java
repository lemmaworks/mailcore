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

package org.apache.james.util.dbcp;

import org.apache.avalon.excalibur.datasource.DataSourceComponent;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.james.util.mordred.SQLBase;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * <p>
 * This is a reliable DataSource implementation, based on the pooling logic provided by <a
 * href="http://jakarta.apache.org/commons/dbcp.html">DBCP</a> and the configuration found in
 * Avalon's excalibur code.
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
 * <p>
 * These configuration settings are available:
 * <ul>
 * <li><b>driver</b> - The class name of the JDBC driver</li>
 * <li><b>dburl</b> - The JDBC URL for this connection</li>
 * <li><b>user</b> - The username to use for this connection</li>
 * <li><b>password</b> - The password to use for this connection</li>
 * <li><b>keep-alive</b> - The SQL query that will be used to validate connections from this pool before returning them to the caller.  If specified, this query <strong>MUST</strong> be an SQL SELECT statement that returns at least one row.</li>
 * <li><b>max</b> - The maximum number of active connections allowed in the pool. 0 means no limit. (default 2)</li>
 * <li><b>max_idle</b> - The maximum number of idle connections.  0 means no limit.  (default 0)</li>
 * <li><b>initial_size</b> -  The initial number of connections that are created when the pool is started. (default 0)</li>
 * <li><b>min_idle</b> -  The minimum number of active connections that can remain idle in the pool, without extra ones being created, or zero to create none. (default 0)</li>
 * <li><b>max_wait</b> -  The maximum number of milliseconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing an exception, or -1 to wait indefinitely. (default -1)</li>
 * <li><b>testOnBorrow</b> -  The indication of whether objects will be validated before being borrowed from the pool. If the object fails to validate, it will be dropped from the pool, and we will attempt to borrow another.  (default true)</li>
 * <li><b>testOnReturn</b> -  The indication of whether objects will be validated before being returned to the pool. (default false)</li>
 * <li><b>testWhileIdle</b> -  The indication of whether objects will be validated by the idle object evictor (if any). If an object fails to validate, it will be dropped from the pool. (default false)</li>
 * <li><b>timeBetweenEvictionRunsMillis</b> -  The number of milliseconds to sleep between runs of the idle object evictor thread. When non-positive, no idle object evictor thread will be run. (default -1)</li>
 * <li><b>numTestsPerEvictionRun</b> -  The number of objects to examine during each run of the idle object evictor thread (if any). (default 3)</li>
 * <li><b>minEvictableIdleTimeMillis</b> -  The minimum amount of time an object may sit idle in the pool before it is eligable for eviction by the idle object evictor (if any). (default 1000 * 60 * 30)</li>
 * </ul>
 *
 * @version CVS $Revision: 494012 $
 */
public class JdbcDataSource extends AbstractLogEnabled
    implements Configurable,
               Disposable,
               DataSourceComponent {

    BasicDataSource source = null;
    //Jdbc2PoolDataSource source = null;
    //PoolingDataSource source = null;

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(final Configuration configuration)
                   throws ConfigurationException {


    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable()
     */
    public void dispose() {
        //Close all database connections
        try {
            source.close();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    /**
     *
     */
    public Connection getConnection() throws SQLException {
        return SQLBase.getConnection();
    }
}
