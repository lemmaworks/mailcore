package org.larry.service.mail;

import org.larry.concurrency.JMXEnabledThreadPoolExecutor;
import org.larry.concurrency.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * User: Larry
 * Date: 7/28/14
 * Time: 5:14 PM
 */
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private static final int MAIL_POOL_SIZE = 8;
    private static final JMXEnabledThreadPoolExecutor sendMailExecutor =
            new JMXEnabledThreadPoolExecutor(MAIL_POOL_SIZE,
                    MAIL_POOL_SIZE,
                    0, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new NamedThreadFactory("SEND_MAIL_DAEMON"));


    private static final Session session;

    static {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "127.0.0.1");
            props.put("mail.smtp.port", "25");
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "false");
            session = Session.getInstance(props);

            session.setDebug(true);

        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void initialize() throws Exception {
        ApacheJamesLoader.initialize();
    }


    public static void sendMail(final String from,
                                final String name,
                                final String sender,
                                final String replyTo,
                                final String[] toAddresses,
                                final String subject,
                                final String content,
                                final String contentType) {


        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    localSMTPSendMail(
                            from,
                            name,
                            sender,
                            replyTo,
                            toAddresses,
                            subject,
                            content,
                            contentType
                    );
                } catch (MailingException e) {
                    LOGGER.warn("Failed to send mail");
                }
            }
        };

        sendMailExecutor.execute(runnable);

    }

    /**
     * Send email to an user in database.
     *
     * @param from
     * @param name
     * @param sender
     * @param replyTo
     * @param subject
     * @param template
     * @param userId
     */
    public static void sendMail(String from,
                                String name,
                                String sender,
                                String replyTo,
                                String subject,
                                String template,
                                int userId) {

        MailTask mailTask = new MailTask(from,
                name,
                sender,
                replyTo,
                subject,
                template,
                userId
        );
        sendMailExecutor.execute(mailTask);

    }

    /**
     * @param from
     * @param name
     * @param sender
     * @param replyTo
     * @param toAddresses
     * @param subject
     * @param content
     * @param contentType
     * @throws MailingException
     */
    public static void localSMTPSendMail(String from,
                                         String name,
                                         String sender,
                                         String replyTo,
                                         String[] toAddresses,
                                         String subject,
                                         String content,
                                         String contentType) throws MailingException {
        /**
         * The email will be first pushed to local disk storage (by Apache James)
         * and then will be sent. Retry factor can be modified in Config.xml
         * See <mailet match="All" class="RemoteDelivery"> in Config.xml
         */
        Transport transport = null;
        try {

            transport = session.getTransport("smtp");
            transport.connect();
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, name));
            message.setSender(new InternetAddress(sender));
            message.setReplyTo(new InternetAddress[]{new InternetAddress(replyTo)});

            InternetAddress[] toInternetAddresses = new InternetAddress[toAddresses.length];
            for (int i = 0; i < toAddresses.length; i++) {
                toInternetAddresses[i] = new InternetAddress(toAddresses[i]);
            }

            message.setRecipients(Message.RecipientType.BCC, toInternetAddresses);
            message.setSubject(subject);
            String _contentType = (contentType == null) ? "text/plain" : contentType;
            message.setContent(content, _contentType);
            transport.sendMessage(message, message.getAllRecipients());
        } catch (Exception e) {
            throw new MailingException(e.getMessage());
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    LOGGER.warn("Cannot close connection properly");
                }
            }
        }
    }


}
