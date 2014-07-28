package org.apache.james.transport.mailets;

import org.apache.mailet.GenericMailet;
import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.MailetException;

import javax.mail.MessagingException;
import javax.mail.internet.ParseException;
import java.util.Collection;

/**
 * User: Larry
 * Date: 4/14/13
 * Time: 8:56 AM
 */
public class Forwarder extends GenericMailet {

    private MailAddress forwardAddress;
    private MailAddress fromAddress;

    /**
     * Initialize the mailet
     *
     * @throws org.apache.mailet.MailetException
     *          if the processor parameter is missing
     */
    public void init() throws MailetException {

        String forward = getInitParameter("forward");
        String from = getInitParameter("from");

        if(forward == "" || from == "")
            throw new MailetException("Forward address and From address cannot be null");

        try{

            this.forwardAddress = new MailAddress(forward);
            this.fromAddress = new MailAddress(from);


        }catch (ParseException e){
            throw new MailetException(e.getMessage());
        }
    }

    @Override
    public void service(Mail mail) throws MessagingException {
        String orginalFrom = mail.getSender().toString();
        //Set headers
        mail.setSender(fromAddress);
        mail.setRemoteAddr("127.0.0.1");

        Collection<MailAddress> recipients = mail.getRecipients();
        recipients.clear();
        recipients.add(forwardAddress);
        mail.setRecipients(recipients);

        mail.getMessage().setSubject("From " + orginalFrom);

        //Set the mail state to "root"
        mail.setState(Mail.DEFAULT);
    }
}
