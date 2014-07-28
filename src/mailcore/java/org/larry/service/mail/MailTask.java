package org.larry.service.mail;

import org.larry.service.sql.SQLBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;

/**
 * User: Larry
 * Date: 3/16/13
 * Time: 8:53 PM
 */
public class MailTask implements Runnable{

    private static final Logger LOGGER = LoggerFactory.getLogger(MailTask.class);

    private final String from;
    private final String name;
    private final String sender;
    private final String replyTo;
    private final String template;
    private final String subject;
    private final int userId;


    public MailTask(String from,
                    String name,
                    String sender,
                    String replyTo,
                    String subject,
                    String template,
                    int userId){
        this.from = from;
        this.name = name;
        this.sender = sender;
        this.subject = subject;
        this.replyTo = replyTo;
        this.userId = userId;
        this.template = template;

    }

    public void run() {
        deliver();
    }

    /**
     * TODO This is not an efficient method to send bulk emails. One
     * optimization can significantly boost the performance:
     * To group receivers into their corresponding domains, and
     * send mail to one domain at a time. This will utilize the
     * connection between Apache James and the target SMTP server
     */
    private void deliver() {

        String clientSQL = "SELECT * FROM client WHERE id = ? ;";

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet res = null;

        try {
            connection = SQLBase.getConnection();
            preparedStatement = connection.prepareStatement(clientSQL);
            preparedStatement.setInt(1, userId);
            res = preparedStatement.executeQuery();

            if (!res.next()) {
                LOGGER.warn("Cannot get info for clientId: " + userId);
                return;
            }

            UserInfo userInfo = UserInfo.parse(res);


            Date today = new Date();

            if (userInfo.getNotContactUntil() != null &&
                    userInfo.getNotContactUntil().compareTo(today) > 0) {
                //we cannot contact this person yet
                LOGGER.info("cannot contact clientId: " + userId + ", reason: not allow to contact until fixed date");
                return;
            }


            String content = MailTemplate.convert(template, userInfo);
            MailService.sendMail(
                    from,
                    name,
                    sender,
                    replyTo,
                    new String[]{userInfo.getEmail()},
                    subject,
                    content,
                    "text/plain");


        } catch (SQLException e) {
            LOGGER.warn("failed to get client info: id = " + userId + ", error: " + e.getMessage());
            
        }  finally {
            try {
                
                if (preparedStatement != null)
                    preparedStatement.close();
                    
                if(connection != null)
                    connection.close();
              
            } catch (Exception ee) {
                LOGGER.warn("Cannot close SQL prepare statement or SQL statement or SQL connection");
            }


        }
    }
}
