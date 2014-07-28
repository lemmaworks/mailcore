package org.larry.service.mail;

/**
 * User: Larry
 * Date: 7/28/14
 * Time: 8:15 PM
 */
public class Test {
    /**
     * TEST
     */
    public static void main(String[] args) {
        try {
            MailService.initialize();

            String from = "xyz@testDomain.com";
            String name = "XYZ";
            String sender = "no-reply@testDomain.com";
            String replyTo = "test@outlook.com";
            String receiver = "testEmail@gmail.com";
            String subject = "Hi";
            String content = "Dear X, \n" +
                    "We are pleased to let's you know that your item now is available to purchase.";


            MailService.sendMail(
                    from,
                    name,
                    sender,
                    replyTo,
                    new String[]{receiver},
                    subject,
                    content,
                    null
            );

        } catch (Exception e) {
            System.exit(0);
        }
    }
}
