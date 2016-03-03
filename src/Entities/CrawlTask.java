package Entities;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Created by Khanh Nguyen on 3/3/2016.
 */
public class CrawlTask {

    private String url;
    private MongoCollection<Document> collection;
    private ForumConfig forumConfig;

    public CrawlTask(String url, MongoCollection<Document> collection, ForumConfig forumConfig) {
        this.url = url;
        this.collection = collection;
        this.forumConfig = forumConfig;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MongoCollection<Document> getCollection() {
        return collection;
    }

    public void setCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public ForumConfig getForumConfig() {
        return forumConfig;
    }

    public void setForumConfig(ForumConfig forumConfig) {
        this.forumConfig = forumConfig;
    }
}
