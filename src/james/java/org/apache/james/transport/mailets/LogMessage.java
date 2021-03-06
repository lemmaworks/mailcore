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

package org.apache.james.transport.mailets;

import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.james.core.MailImpl;
import org.apache.mailet.GenericMailet;
import org.apache.mailet.Mail;

import java.io.InputStream;
import java.lang.StringBuffer;

/**
 * Logs Message Headers and/or Body.
 * If the "passThrough" in confs is true the mail will be left untouched in
 * the pipe. If false will be destroyed.  Default is true.
 *
 * @version This is $Revision: 1.8.4.2 $
 */
public class LogMessage extends GenericMailet {

    /**
     * Whether this mailet should allow mails to be processed by additional mailets
     * or mark it as finished.
     */
    private boolean passThrough = true;
    private boolean headers = true;
    private boolean body = true;
    private int bodyMax = 0;
    private String comment = null;

    /**
     * Initialize the mailet, loading configuration information.
     */
    public void init() {
        try {
            passThrough = (getInitParameter("passThrough") == null) ? true : new Boolean(getInitParameter("passThrough")).booleanValue();
            headers = (getInitParameter("headers") == null) ? true : new Boolean(getInitParameter("headers")).booleanValue();
            body = (getInitParameter("body") == null) ? true : new Boolean(getInitParameter("body")).booleanValue();
            bodyMax = (getInitParameter("maxBody") == null) ? 0 : Integer.parseInt(getInitParameter("maxBody"));
            comment = getInitParameter("comment");
        } catch (Exception e) {
            // Ignore exception, default to true
        }
    }

    /**
     * Log a particular message
     *
     * @param mail the mail to process
     */
    public void service(Mail genericmail) {
        MailImpl mail = (MailImpl)genericmail;
        log(new StringBuffer(160).append("Logging mail ").append(mail.getName()).toString());
        if (comment != null) log(comment);
        try {
            if (headers) log(getMessageHeaders(mail.getMessage()));
            if (body) {
                int len = bodyMax > 0 ? bodyMax : mail.getMessage().getSize();
                StringBuffer text = new StringBuffer(len);
                InputStream is = mail.getMessage().getRawInputStream();
                byte[] buf = new byte[1024];
                int read = 0;
                while (text.length() < len && (read = is.read(buf)) > -1) {
                    text.append(new String(buf, 0, Math.min(read, len - text.length())));
                }
                log(text.toString());
            }
        }
        catch (MessagingException e) {
            log("Error logging message.", e);
        }
        catch (java.io.IOException e) {
            log("Error logging message.", e);
        }
        if (!passThrough) {
            mail.setState(Mail.GHOST);
        }
    }

    /**
     * Utility method for obtaining a string representation of a
     * Message's headers
     */
    private String getMessageHeaders(MimeMessage message) throws MessagingException {
        Enumeration heads = message.getAllHeaderLines();
        StringBuffer headBuffer = new StringBuffer(1024).append("\n");
        while(heads.hasMoreElements()) {
            headBuffer.append(heads.nextElement().toString()).append("\n");
        }
        return headBuffer.toString();
    }

    /**
     * Return a string describing this mailet.
     *
     * @return a string describing this mailet
     */
    public String getMailetInfo() {
        return "LogHeaders Mailet";
    }
}
