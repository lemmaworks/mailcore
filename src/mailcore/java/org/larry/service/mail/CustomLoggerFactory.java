package org.larry.service.mail;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Larry
 * Date: 4/9/13
 * Time: 5:01 PM
 * <p/>
 * <p/>
 */
public class CustomLoggerFactory {

    public static void init() {

        org.apache.log4j.Logger.getRootLogger().getLoggerRepository().resetConfiguration();

        org.apache.log4j.Logger.getRootLogger().setLevel(Level.TRACE);

        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        /**
         * For comprehensive info, see The Complete Log4j Manual, p137
         *
         * [%C{8}]
         *          [   --> character
         *          %c  --> print class name
         *          40  --> minimum width, pad left
         *          {8} --> up to 7 level of the package name, {1} mean the class name only
         *          This flag is considered to be very slow
         *
         */
        String PATTERN = "%-25c{1} %-5p %d{HH:mm:ss} %m%n";
        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(Level.DEBUG);
        console.activateOptions();
        //add appender to any Logger (here is root)
        org.apache.log4j.Logger.getRootLogger().addAppender(console);

        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile("Log.txt");
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.DEBUG);
        fa.setAppend(true);
        fa.activateOptions();

        //add appender to any Logger(here is root)
        org.apache.log4j.Logger.getRootLogger().addAppender(fa);


    }

    public static Logger getLogger(String className) {
        return LoggerFactory.getLogger(className);
    }

    public static Logger getLogger(Class clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}
