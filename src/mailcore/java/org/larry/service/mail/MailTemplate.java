package org.larry.service.mail;

/**
 * User: Larry
 * Date: 3/27/13
 * Time: 8:39 PM
 */
public class MailTemplate {


    private static final String TITLE = "$title";
    private static final String FIRST_NAME = "$firstName";
    private static final String LAST_NAME = "$lastName";
    private static final String PHONE = "$phone";
    private static final String EMAIL = "$email";


    public static String convert(String template, UserInfo clientInfo) {
        String res = template.substring(0, template.length());

        res  = res.replace(TITLE, clientInfo.getTitle());
        res = res.replace(FIRST_NAME, clientInfo.getFirstName());
        res = res.replace(LAST_NAME, clientInfo.getLastName());
        res = res.replace(PHONE, clientInfo.getPhone());
        res = res.replace(EMAIL, clientInfo.getEmail());

        return res;
    }

}
