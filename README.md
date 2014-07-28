mailcore
========

# About


MailCore is the underlying mailing service of a mass mailing system. By using MailCore, you can send emails using your organization's domain name. 


MailCore uses a modified version of Apache James as the SMTP server. Any email to your organization domain will be forwarded to a custom email address (see Requirements & Installation) 


MailCore only loads key components of Apache James and neglects unnecessary features (POP3, IMAP, ...).


# Requirements & Installation


1. Modify config.xml to reflect your organization's domain name
2. Email Forwarder: MailCore supports email forwarder. Modify config.xml as following
       ```xml
       <mailet match="HostIsLocal" class="Forwarder">
              <from>elink.mta@server-name.com</from>
              <forward>larry.mta@gmail.com</forward>
       </mailet>
       ```  
      
       Any email to your domain address will be modified and sent to the "forward" address. The "from's field" is the new from's header of the email, and its domain must be your organization domain. The subject of the email will have the format "From xyz@domain.com" where xyz.domain.com is the original sender
        
       See org.apache.james.transport.mailets.Forwarder for further information
       
3. MailCore includes a sample MailTask where email's contents can be pulled from SQL database.

4. See org.larry.service.mail.Test for usages


# License
[Apache License v2](http://www.apache.org/licenses/LICENSE-2.0.html)
