package Entities;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Created by Khanh Nguyen on 1/13/2016.
 */
public class ForumPost {
    private String threadUrl;
    private String boardUrl;
    private String postId;
    private String postUrl;
    private String userName;
    private String postContent;
    private String postTime;
    private boolean hasQuote;
    private boolean isUpdated;

    public ForumPost() {}

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
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

    public String getThreadUrl() {
        return threadUrl;
    }

    public void setThreadUrl(String threadUrl) {
        this.threadUrl = threadUrl;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }

    public String getBoardUrl() {
        return boardUrl;
    }

    public void setBoardUrl(String boardUrl) {
        this.boardUrl = boardUrl;
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
                .append("threadUrl", this.threadUrl)
                .append("boardUrl", this.boardUrl)
                .append("postTime", this.postTime)
                .append("postUrl", this.postUrl)
                .append("postUser", this.userName)
                .append("hasQuote", this.hasQuote)
                .append("postContent", this.postContent)
                .append("postUpdated", this.isUpdated);
    }

    public void addPostToDB(MongoCollection<Document> collection) {
        String postId = this.postId;
        org.bson.Document doc = collection.find(new org.bson.Document("_id", postId)).first();
        if (doc == null) {
            collection.insertOne(this.extractPostBson());
        } else {
            collection.replaceOne(
                    new org.bson.Document("_id", postId),
                    this.extractPostBson()
            );
        }
    }
}
