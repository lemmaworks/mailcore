package org.larry.service.mail;

import org.apache.avalon.framework.logger.Logger;

/**
 * User: Larry
 * Date: 4/9/13
 * Time: 3:53 PM
 */
public class Log4jWrapper implements Logger {



    private boolean m_debugEnabled = true;
    private org.slf4j.Logger logger;

    public Log4jWrapper(Class clazz) {
        this.logger = CustomLoggerFactory.getLogger(clazz.getName());

    }

    public Log4jWrapper(String className) {
        this.logger = CustomLoggerFactory.getLogger(className);
    }

    public void debug(java.lang.String string) {
        logger.debug(string);
    }

    public void debug(java.lang.String string, java.lang.Throwable throwable) {
        logger.debug(string);
        throwable.printStackTrace();
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void disableDebug() {

    }

    public void info(java.lang.String string) {
        logger.info(string);
    }

    public void info(java.lang.String string, java.lang.Throwable throwable) {
        logger.info(string);
        throwable.printStackTrace();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void warn(java.lang.String string) {
        logger.warn(string);
    }

    public void warn(java.lang.String string, java.lang.Throwable throwable) {
        logger.warn(string);
        throwable.printStackTrace();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void error(java.lang.String string) {
        logger.error(string);
    }

    public void error(java.lang.String string, java.lang.Throwable throwable) {
        logger.error(string);
        throwable.printStackTrace();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public void fatalError(java.lang.String string) {
        logger.error(string);
    }

    public void fatalError(java.lang.String string, java.lang.Throwable throwable) {
        logger.error(string);
        throwable.printStackTrace();
    }

    public boolean isFatalErrorEnabled() {
        return true;
    }

    public org.apache.avalon.framework.logger.Logger getChildLogger(java.lang.String string) {
        return this;
    }


    //---------------------------------------------------------------------------------------
    //  TEST
    //---------------------------------------------------------------------------------------

    public static void main(String[] args) {
        Log4jWrapper logger = new Log4jWrapper(Log4jWrapper.class);

        System.out.println("DEBUG:\t" + logger.isDebugEnabled());
        System.out.println("INFO:\t" + logger.isInfoEnabled());
        System.out.println("WARN:\t" + logger.isWarnEnabled());

        logger.debug(" test");
        logger.debug(" test2");
    }


}

