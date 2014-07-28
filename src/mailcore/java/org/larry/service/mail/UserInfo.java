package org.larry.service.mail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * User: Larry
 * Date: 7/28/14
 * Time: 3:28 PM
 */
public class UserInfo {

    private final String title;
    private final String firstName;
    private final String lastName;
    private final Date notContactUntil;
    private final Date lastSentMail;
    private final boolean isSubscriber;
    private final String type;
    private final String email;
    private final String phone;
    private final int id;

    public UserInfo(String title,
                    String firstName,
                    String lastName,
                    Date notContactUntil,
                    Date lastSentMail,
                    boolean isSubscriber,
                    String type,
                    String email,
                    String phone,
                    int id) {

        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.notContactUntil = notContactUntil;
        this.lastSentMail = lastSentMail;
        this.isSubscriber = isSubscriber;
        this.type = type;
        this.email = email;
        this.phone = phone;
        this.id = id;
    }


    public static UserInfo parse(ResultSet resultSet) throws SQLException {
        String title = stringOrElse(resultSet, "title", "");
        String firstName = stringOrElse(resultSet, "firstName", "");
        String lastName = stringOrElse(resultSet, "lastName", "");
        String email = stringOrElse(resultSet, "email", "(your undisclosed email)");
        String phone = stringOrElse(resultSet, "phone", "(your undisclosed phone number)");
        Date notContactUntil = resultSet.getDate("notContactUntil");
        Date lastSentMail = resultSet.getDate("lastSentMail");
        String type = stringOrElse(resultSet, "type", "Personal");
        boolean isSubscriber = resultSet.getInt("subscribe") > 0;
        int id = resultSet.getInt("id");


        return new UserInfo(title,
                firstName,
                lastName,
                notContactUntil,
                lastSentMail,
                isSubscriber,
                type,
                email,
                phone,
                id);

    }

    private static String stringOrElse(ResultSet resultSet, String name, String defaultValue)
            throws SQLException {
        String res = resultSet.getString(name);
        if (res == null)
            return defaultValue;
        else
            return res;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getNotContactUntil() {
        return notContactUntil;
    }

    public Date getLastSentMail() {
        return lastSentMail;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public String getType() {
        return type;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public int getId() {
        return id;
    }
}
