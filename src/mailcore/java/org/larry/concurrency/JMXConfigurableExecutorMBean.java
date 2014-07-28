package org.larry.concurrency;

/**
 * Larry Lab 2010
 * User: Larry
 * Date: Nov 2, 2010
 * Time: 5:28:10 PM
 */
public interface JMXConfigurableExecutorMBean extends JMXEnabledExecutorMBean {
    void setCorePoolSize(int n);

    int getCorePoolSize();
}
