package Entities;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Created by Khanh Nguyen on 1/13/2016.
 */
public class ForumThread {

    private String boardUrl;
    private String threadUrl;
    private boolean isSticky;
    private String threadName;
    private String threadCreator;
    private String lastPostTime;
    private String lastPostUser;
    private int replies;
    private int views;
    private boolean isUpdated;

    public ForumThread() {}

    public ForumThread(String threadUrl) {
        this.threadUrl = threadUrl;
        this.isUpdated = false;
    }

    public String getBoardUrl() {
        return boardUrl;
    }

    public void setBoardUrl(String boardUrl) {
        this.boardUrl = boardUrl;
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

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean sticky) {
        isSticky = sticky;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getThreadCreator() {
        return threadCreator;
    }

    public void setThreadCreator(String threadCreator) {
        this.threadCreator = threadCreator;
    }

    public String getLastPostTime() {
        return lastPostTime;
    }

    public void setLastPostTime(String lastPostTime) {
        this.lastPostTime = lastPostTime;
    }

    public String getLastPostUser() {
        return lastPostUser;
    }

    public void setLastPostUser(String lastPostUser) {
        this.lastPostUser = lastPostUser;
    }

    public int getReplies() {
        return replies;
    }

    public void setReplies(int replies) {
        this.replies = replies;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void printThread() {
        System.out.println("THREAD INFO");
        System.out.println("ThreadUrl: " + this.threadUrl);
        System.out.println("Up to date: " + this.isUpdated);
        System.out.println("ThreadName: " + this.threadName);
        System.out.println("Sticky: " + this.isSticky);
        System.out.println("Creator: " + this.threadCreator);
        System.out.println("Last posted: " + this.lastPostTime);
        System.out.println("Last post user: " + this.lastPostUser);
        System.out.println("Replies: " + this.replies);
        System.out.println("Views: " + this.views);

    }

    public Document extractThreadBson() {
        return new Document("_id", this.threadUrl)
                .append("threadName", this.threadName)
                .append("threadCreator", this.threadCreator)
                .append("isThreadSticky", this.isSticky)
                .append("isThreadUpdated", this.isUpdated)
                .append("threadLastPostTime", this.lastPostTime)
                .append("threadLastPostUser", this.lastPostUser)
                .append("threadReplies", this.replies)
                .append("threadViews", this.views);
    }

    public void addThreadToDB(MongoCollection<Document> collection) {
        String threadUrl = this.threadUrl;
        org.bson.Document doc = collection.find(new org.bson.Document("_id", threadUrl)).first();
        if (doc==null) {
            collection.insertOne(
                    this.extractThreadBson()
            );
            //System.out.println("Thread added.");
        } else {
            collection.replaceOne(
                    new org.bson.Document("_id", threadUrl),
                    this.extractThreadBson()
            );
            //System.out.println("Thread updated.");
        }
    }
}
