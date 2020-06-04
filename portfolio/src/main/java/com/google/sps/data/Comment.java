/** Class containing comment object*/
package com.google.sps.data;
import java.util.Date;

public final class Comment {

    private final String content;
    private final String author;
    private final Date date;
    private final String emotion;

    public Comment(String content, String author, Date date, String emotion) {
        this.content = content;
        this.author = author; 
        this.date = date;
        this.emotion = emotion;
    }

    public String getContent() {
        return content;
    }

	public String getAuthor() {
        return author;
    }

    public Date getDate() {
        return date;
    }

    public String getEmotion() {
        return emotion;
    }

}
