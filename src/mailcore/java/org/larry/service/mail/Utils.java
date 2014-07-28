package org.larry.service.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * User: Larry
 * Date: 3/19/13
 * Time: 3:52 PM
 */
public class Utils {


    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final Pattern mailPattern;

    static {
        mailPattern = Pattern.compile(EMAIL_PATTERN);
    }


    //For example test@Gmail.com --> return "gmail"
    public static String getEmailProvider(String email) {

        String s = email.trim();
        s = s.substring(s.indexOf("@") + 1, s.length());
        return s.split("\\.")[0].toLowerCase();

    }


    public static boolean isValidEmail(String email) {
        return mailPattern.matcher(email).matches();
    }


    public static String getText(Part p) throws MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

    public static String getSubject(Message message) throws MessagingException {
        return getSubject(message.getSubject());
    }

    public static String getSubject(String subject) {
        String str = escapeSpecialChars(subject);

        if (str.toLowerCase().trim().indexOf("re:") == 0) {
            String s = str.trim().substring(3, str.length());;
            return getSubject(s);
        } else
            return str.trim();
    }

    /**
     * MySQL recognizes the following escape sequences.
     * \0  An ASCII NUL (0x00) character.
     * \'  A single quote (“'”) character.
     * \"  A double quote (“"”) character.
     * \b  A backspace character.
     * \n  A newline (linefeed) character.
     * \r  A carriage return character.
     * \t  A tab character.
     * \Z  ASCII 26 (Control-Z). See note following the table.
     * \\  A backslash (“\”) character.
     * \%  A “%” character. See note following the table.
     * \_  A “_” character. See note following the table.
     *
     * @param subject
     * @return
     */
    private static String escapeSpecialChars(String subject) {
        String s = subject.replace("'", "\\'");
        s = s.replace("\"", "\\\"");
        s = s.replace("\\0", "\\\0");
        s = s.replace("\\b", "\\\b");
        s = s.replace("\\n", "\\\n");
        s = s.replace("\\r", "\\\r");
        s = s.replace("\\t", "\\\t");
        s = s.replace("%", "\\%");
        return s;
    }

    public static boolean isRe(String subject) {
        if (subject.toLowerCase().trim().indexOf("re:") == 0) {
            return true;
        }

        return false;
    }


}
