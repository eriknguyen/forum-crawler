package Entities;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

/**
 * Created by Khanh Nguyen on 3/3/2016.
 */
public class CrawlTask {

    private String url;
    private MongoDatabase db;
    private ForumConfig forumConfig;

    public CrawlTask(String url, MongoDatabase db, ForumConfig forumConfig) {
        this.url = url;
        this.db = db;
        this.forumConfig = forumConfig;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MongoDatabase getDb() {
        return db;
    }

    public void setDb(MongoDatabase db) {
        this.db = db;
    }

    public ForumConfig getForumConfig() {
        return forumConfig;
    }

    public void setForumConfig(ForumConfig forumConfig) {
        this.forumConfig = forumConfig;
    }
}
