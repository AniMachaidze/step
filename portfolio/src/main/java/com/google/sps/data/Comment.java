/** Class containing comment object*/
package com.google.sps.data;
import java.util.Date;

public final class Comment {

    private final String content;
    private final String userName;
    private final String userEmail;
    private final Date date;
    private final String emotion;
    private final boolean isAbleToDelete;
    private final String id;

    public Comment(String content, String userName, String userEmail,
        Date date, String emotion, boolean isAbleToDelete, String id) {
        this.content = content;
        this.userName = userName;
        this.userEmail = userEmail;
        this.date = date;
        this.emotion = emotion;
        this.isAbleToDelete = isAbleToDelete;
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public Date getDate() {
        return date;
    }

    public String getEmotion() {
        return emotion;
    }

    public boolean getIseAbleToDelete() {
        return isAbleToDelete;
    }

    public String getId() {
        return id;
    }

}
