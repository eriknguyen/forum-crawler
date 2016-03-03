package Entities;

import org.bson.Document;

/**
 * Created by Khanh Nguyen on 1/13/2016.
 */
public class ForumPost {
    private ForumThread thread;
    private String postId;
    private String postUrl;
    private String userName;
    private String postContent;
    private String postTime;
    private boolean hasQuote;

    public ForumPost() {}

    public ForumPost(ForumThread thread) {
        this.thread = thread;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public ForumThread getThread() {
        return thread;
    }

    public void setThread(ForumThread thread) {
        this.thread = thread;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public boolean isHasQuote() {
        return hasQuote;
    }

    public void setHasQuote(boolean hasQuote) {
        this.hasQuote = hasQuote;
    }

    public void printPost() {
        System.out.println("POST " + this.postId + ": ");
        System.out.println("Posted time: " + this.postTime);
        System.out.println("User: " + this.userName);
        System.out.println("Has quote: " + this.hasQuote);
        System.out.println("Content: " + this.postContent);
    }

    public Document extractPostBson() {
        return new Document("_id", this.postId)
                .append("postTime", this.postTime)
                .append("postUrl", this.postUrl)
                .append("postUser", this.userName)
                .append("hasQuote", this.hasQuote)
                .append("postContent", this.postContent);
    }
}
