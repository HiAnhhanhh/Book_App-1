package com.example.book_app.models;

public class CommentModel {
    String id,uid,timestamp, comment, bookId;

    public CommentModel() {
    }

    public CommentModel(String id, String uid, String timestamp, String comment, String bookId) {
        this.id = id;
        this.uid = uid;
        this.timestamp = timestamp;
        this.comment = comment;
        this.bookId = bookId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
