package org.larry.service.mail;

import org.apache.avalon.cornerstone.blocks.sockets.DefaultSocketManager;
import org.apache.avalon.cornerstone.blocks.threads.DefaultThreadManager;
import org.apache.avalon.cornerstone.services.sockets.SocketManager;
import org.apache.avalon.cornerstone.services.store.Store;
import org.apache.avalon.cornerstone.services.threads.ThreadManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.james.James;
import org.apache.james.context.AvalonContextConstants;
import org.apache.james.core.AvalonMailStore;
import org.apache.james.core.AvalonUsersStore;
import org.apache.james.core.LocalUsersRepository;
import org.apache.james.dnsserver.DNSServer;
import org.apache.james.mailrepository.MailStoreSpoolRepository;
import org.apache.james.services.*;
import org.apache.james.smtpserver.SMTPServer;
import org.apache.james.transport.JamesMailetLoader;
import org.apache.james.transport.JamesMatcherLoader;
import org.apache.james.transport.JamesSpoolManager;
import org.apache.james.util.connection.SimpleConnectionManager;
import org.slf4j.Logger;

import java.io.File;

/**
 * User: Larry
 * Date: 7/28/14
 * Time: 5:11 PM
 */
public class ApacheJamesLoader {
    private static final Logger LOGGER = CustomLoggerFactory.getLogger(ApacheJamesLoader.class);

    public static void initialize() throws Exception {

        Configuration configuration = null;

        DefaultConfigurationBuilder configurationBuilder = new DefaultConfigurationBuilder();
        try {
            configuration = configurationBuilder.buildFromFile("etc/config.xml");
        } catch (Exception e) {
            System.out.println("Error, failed to read main configuration\n" + e.getMessage());
            System.exit(1);
        }

        DefaultServiceManager serviceManager = new DefaultServiceManager();
        DefaultContext defaultContext = new DefaultContext();
        defaultContext.put(AvalonContextConstants.APPLICATION_HOME, new File("."));

        //------------------------------------------------------------------------------------------
        //  THREAD MANAGER
        //
        //  Provide thread-pools. Currently only one pool used by the application
        //
        //------------------------------------------------------------------------------------------

        DefaultThreadManager threadManager = new DefaultThreadManager();
        threadManager.enableLogging(new Log4jWrapper(DefaultThreadManager.class));
        threadManager.configure(configuration.getChild("thread-manager"));

        serviceManager.put(ThreadManager.ROLE, threadManager);
        LOGGER.info("Init ThreadManager ... Done");

        //------------------------------------------------------------------------------------------
        //  SOCKET MANAGER
        //
        //------------------------------------------------------------------------------------------

        DefaultSocketManager defaultSocketManager = new DefaultSocketManager();
        defaultSocketManager.enableLogging(new Log4jWrapper(DefaultSocketManager.class));
        defaultSocketManager.contextualize(defaultContext);
        defaultSocketManager.configure(configuration.getChild("sockets"));
        defaultSocketManager.initialize();

        serviceManager.put(SocketManager.ROLE, defaultSocketManager);
        LOGGER.info("Init SocketManager ... Done");

        //------------------------------------------------------------------------------------------
        // SIMPLE CONNECTION MANAGER
        //
        //
        //------------------------------------------------------------------------------------------
        SimpleConnectionManager simpleConnectionManager = new SimpleConnectionManager();
        simpleConnectionManager.enableLogging(new Log4jWrapper(SimpleConnectionManager.class));
        simpleConnectionManager.service(serviceManager);
        simpleConnectionManager.configure(configuration.getChild("connections"));

        serviceManager.put(JamesConnectionManager.ROLE, simpleConnectionManager);
        LOGGER.info("Init JamesConnectionManager ... Done");

        //------------------------------------------------------------------------------------------
        // STORE
        //
        //
        //------------------------------------------------------------------------------------------
        AvalonMailStore avalonMailStore = new AvalonMailStore();
        avalonMailStore.enableLogging(new Log4jWrapper(AvalonMailStore.class));
        avalonMailStore.contextualize(defaultContext);
        avalonMailStore.service(serviceManager);
        avalonMailStore.configure(configuration.getChild("mailstore"));
        avalonMailStore.initialize();

        serviceManager.put(Store.ROLE, avalonMailStore);
        LOGGER.info("Init Store ... Done");


        //------------------------------------------------------------------------------------------
        // USER STORE
        //
        //------------------------------------------------------------------------------------------
        AvalonUsersStore avalonUsersStore = new AvalonUsersStore();
        avalonUsersStore.enableLogging(new Log4jWrapper(AvalonUsersStore.class));
        avalonUsersStore.contextualize(defaultContext);
        avalonUsersStore.service(serviceManager);
        avalonUsersStore.configure(configuration.getChild("users-store"));
        avalonUsersStore.initialize();

        serviceManager.put(UsersStore.ROLE, avalonUsersStore);
        LOGGER.info("Init UsersStore ... Done");


        //------------------------------------------------------------------------------------------
        //  LOCAL USER REPOSITORY
        //
        //
        //------------------------------------------------------------------------------------------
        LocalUsersRepository localUsersRepository = new LocalUsersRepository();
        localUsersRepository.service(serviceManager);
        localUsersRepository.initialize();
        localUsersRepository.addUser("int2", "wonderful");
        localUsersRepository.addUser("postmaster", "wonderful");

        serviceManager.put(UsersRepository.ROLE, localUsersRepository);
        LOGGER.info("Init Local UsersRepository ... Done");

        //------------------------------------------------------------------------------------------
        // SPOOL REPOSITORY
        //
        //
        //------------------------------------------------------------------------------------------
        MailStoreSpoolRepository mailStoreSpoolRepository = new MailStoreSpoolRepository();
        mailStoreSpoolRepository.enableLogging(new Log4jWrapper(MailStoreSpoolRepository.class));
        mailStoreSpoolRepository.service(serviceManager);
        mailStoreSpoolRepository.configure(configuration.getChild("spoolrepository"));
        mailStoreSpoolRepository.initialize();

        serviceManager.put(SpoolRepository.ROLE, mailStoreSpoolRepository);
        LOGGER.info("Init SpoolRepository ... Done");


        //------------------------------------------------------------------------------------------
        // DNS Server
        //
        //
        //------------------------------------------------------------------------------------------
        DNSServer dnsServer = new DNSServer();
        dnsServer.enableLogging(new Log4jWrapper(DNSServer.class));
        dnsServer.configure(configuration.getChild("dnsserver"));
        dnsServer.initialize();

        serviceManager.put(DNSServer.ROLE, dnsServer);
        LOGGER.info("Init DNSService ... Done");


        //------------------------------------------------------------------------------------------
        // JAMES
        //
        //
        //------------------------------------------------------------------------------------------
        James james = new James();
        james.enableLogging(new Log4jWrapper(James.class));
        james.contextualize(defaultContext);
        james.service(serviceManager);
        james.configure(configuration.getChild("James"));
        james.initialize();

        serviceManager.put(MailServer.ROLE, james);
        serviceManager.put("org.apache.mailet.MailetContext", james);
        LOGGER.info("Init James ... Done");

        //------------------------------------------------------------------------------------------
        // JAMES MAILET LOADER
        //
        //
        //------------------------------------------------------------------------------------------
        JamesMailetLoader jamesMailetLoader = new JamesMailetLoader();
        jamesMailetLoader.enableLogging(new Log4jWrapper(JamesMailetLoader.class));
        jamesMailetLoader.contextualize(defaultContext);
        jamesMailetLoader.service(serviceManager);
        jamesMailetLoader.configure(configuration.getChild("mailetpackages"));
        jamesMailetLoader.initialize();

        serviceManager.put(MailetLoader.ROLE, jamesMailetLoader);
        LOGGER.info("Init James Mailet Loader ... Done");


        //------------------------------------------------------------------------------------------
        // JAMES MATCHER LOADER
        //
        //
        //------------------------------------------------------------------------------------------
        JamesMatcherLoader jamesMatcherLoader = new JamesMatcherLoader();
        jamesMatcherLoader.enableLogging(new Log4jWrapper(JamesMatcherLoader.class));
        jamesMatcherLoader.contextualize(defaultContext);
        jamesMatcherLoader.service(serviceManager);
        jamesMatcherLoader.configure(configuration.getChild("matcherpackages"));
        jamesMatcherLoader.initialize();

        serviceManager.put(MatcherLoader.ROLE, jamesMatcherLoader);
        LOGGER.info("Init James Matcher Loader ... Done");


        //------------------------------------------------------------------------------------------
        // SPOOL MANAGER
        //
        //
        //------------------------------------------------------------------------------------------
        JamesSpoolManager jamesSpoolManager = new JamesSpoolManager();
        jamesSpoolManager.enableLogging(new Log4jWrapper(JamesSpoolManager.class));
        jamesSpoolManager.service(serviceManager);
        jamesSpoolManager.configure(configuration.getChild("spoolmanager"));
        jamesSpoolManager.initialize();

        //don't need to export anything to serviceManager, this is a top lvl block
        LOGGER.info("Init JamesSpoolManager ... Done");


        //------------------------------------------------------------------------------------------
        // SMTP Server
        //
        //
        //------------------------------------------------------------------------------------------
        SMTPServer smtpServer = new SMTPServer();
        smtpServer.enableLogging(new Log4jWrapper(SMTPServer.class));
        smtpServer.service(serviceManager);
        smtpServer.configure(configuration.getChild("smtpserver"));
        smtpServer.initialize();


        //don't need to export anything to serviceManager, this is a top lvl block
        LOGGER.info("Init SMTP Server ... Done");
    }
}
