package org.larry.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * ****************************************************************
 * Yuppp Inc (2010-2011), INT2 LAB (2003-2011)
 * User: Larry
 * Jun 19, 2011
 * 12:15:29 AM
 * Description:
 * ****************************************************************
 */
public class Configuration {
    private static final String CONFIG_FILE = "config.properties";

    private static final String MYSQL_SERVER_IP;
    private static final int MYSQL_SERVER_PORT;
    private static final String MYSQL_SERVER_USER;
    private static final String MYSQL_SERVER_PASSWORD;
    private static final String MYSQL_DATABASE;
    private static final boolean DEV_MODE;

    static {

        Properties prop = new Properties();
        try {
            InputStream in = new FileInputStream(CONFIG_FILE);
            prop.load(in);

            DEV_MODE = boolOrElse(prop, "devMode", true);
            MYSQL_SERVER_IP = stringOrElse(prop, "ip", "127.0.0.1");
            MYSQL_SERVER_PORT = intOrElse(prop, "port", 3306);
            MYSQL_SERVER_USER = stringOrElse(prop, "user", "root");
            MYSQL_SERVER_PASSWORD = stringOrElse(prop, "password", "123456");
            MYSQL_DATABASE = stringOrElse(prop, "database", "edx");
            in.close();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Fatal Error: failed to read configuration file\n" + e);
        }
    }

    //support routine to parse Properties
    static boolean boolOrElse(Properties prop, String name, boolean defaultValue) {
        String value = prop.getProperty(name);
        if (value == null)
            return defaultValue;
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static int intOrElse(Properties prop, String name, int defaultValue) {
        String value = prop.getProperty(name);
        try {
            if (value == null)
                return defaultValue;

            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    static String stringOrElse(Properties prop, String name, String defaultValue) {
        String value = prop.getProperty(name);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    static String stringOrException(Properties prop, String name) throws Exception {
        String value = prop.getProperty(name);
        if (value == null)
            throw new Exception("[Configuration] Critical error: property " + name + " does not exist in yuppp.properties");

        return value;
    }

    public static String getMysqlServerIp() {
        return MYSQL_SERVER_IP;
    }

    public static int getMysqlServerPort() {
        return MYSQL_SERVER_PORT;
    }

    public static String getMysqlServerUser() {
        return MYSQL_SERVER_USER;
    }

    public static String getMysqlServerPassword() {
        return MYSQL_SERVER_PASSWORD;
    }

    public static String getMysqlDatabase() {
        return MYSQL_DATABASE;
    }

    public static boolean isDevMode() {
        return DEV_MODE;
    }


}
