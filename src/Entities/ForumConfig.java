package Entities;

import org.bson.Document;

/**
 * Created by Khanh Nguyen on 2/23/2016.
 */
public class ForumConfig {

    private String forumId;
    private String url;
    private String collectionName;
    private String boardSelector;
    private String nextButton;
    private boolean useTimeAttribute;
    private String timeAttributeName;
    private boolean postNewToOld;

    private String threadSelector;
    private String threadTitle;
    private String threadLastPostTime;
    private String threadLastPostUser;
    private String stickyClass;
    private String threadCreator;
    private String threadReplies;
    private String threadViews;

    private String postSelector;
    private String postTime;
    private String postUser;
    private String postBody;
    private String postContent;
    private String postQuote;

    public ForumConfig(Document configDoc) {
        this.forumId = configDoc.getString("_id");
        this.url = configDoc.getString("url");
        this.collectionName = configDoc.getString("collectionName");
        this.boardSelector = configDoc.getString("boardSelector");
        this.nextButton = configDoc.getString("nextButtonSelector");
        this.useTimeAttribute = configDoc.getBoolean("useTimeAttribute");
        this.timeAttributeName = configDoc.getString("timeAttributeName");
        this.postNewToOld = configDoc.getBoolean("postNewToOld");

        Document threadSelectorGroup = ((Document) configDoc.get("thread"));
        this.threadSelector = threadSelectorGroup.getString("threadSelector");
        this.threadTitle = threadSelectorGroup.getString("threadTitle");
        this.threadLastPostTime = threadSelectorGroup.getString("lastPostTime");
        this.threadLastPostUser = threadSelectorGroup.getString("lastPostUser");
        this.stickyClass = threadSelectorGroup.getString("stickyClass");
        this.threadCreator = threadSelectorGroup.getString("threadCreator");
        this.threadReplies = threadSelectorGroup.getString("replies");
        this.threadViews = threadSelectorGroup.getString("views");

        Document postSelectorGroup = (Document) configDoc.get("post");
        this.postSelector = postSelectorGroup.getString("postSelector");
        this.postTime = postSelectorGroup.getString("postTime");
        this.postUser = postSelectorGroup.getString("postUser");
        this.postBody = postSelectorGroup.getString("postBody");
        this.postContent = postSelectorGroup.getString("postContent");
        this.postQuote = postSelectorGroup.getString("postQuote");

    }

    public String getForumId() {
        return forumId;
    }

    public void setForumId(String forumId) {
        this.forumId = forumId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getBoardSelector() {
        return boardSelector;
    }

    public void setBoardSelector(String boardSelector) {
        this.boardSelector = boardSelector;
    }

    public String getThreadSelector() {
        return threadSelector;
    }

    public void setThreadSelector(String threadSelector) {
        this.threadSelector = threadSelector;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
    }

    public String getThreadLastPostTime() {
        return threadLastPostTime;
    }

    public void setThreadLastPostTime(String threadLastPostTime) {
        this.threadLastPostTime = threadLastPostTime;
    }

    public String getThreadLastPostUser() {
        return threadLastPostUser;
    }

    public void setThreadLastPostUser(String threadLastPostUser) {
        this.threadLastPostUser = threadLastPostUser;
    }

    public String getStickyClass() {
        return stickyClass;
    }

    public void setStickyClass(String stickyClass) {
        this.stickyClass = stickyClass;
    }

    public String getThreadCreator() {
        return threadCreator;
    }

    public void setThreadCreator(String threadCreator) {
        this.threadCreator = threadCreator;
    }

    public String getThreadReplies() {
        return threadReplies;
    }

    public void setThreadReplies(String threadReplies) {
        this.threadReplies = threadReplies;
    }

    public String getThreadViews() {
        return threadViews;
    }

    public void setThreadViews(String threadViews) {
        this.threadViews = threadViews;
    }

    public String getPostSelector() {
        return postSelector;
    }

    public void setPostSelector(String postSelector) {
        this.postSelector = postSelector;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getPostUser() {
        return postUser;
    }

    public void setPostUser(String postUser) {
        this.postUser = postUser;
    }

    public String getPostBody() {
        return postBody;
    }

    public void setPostBody(String postBody) {
        this.postBody = postBody;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getNextButton() {
        return nextButton;
    }

    public void setNextButton(String nextButton) {
        this.nextButton = nextButton;
    }

    public String getPostQuote() {
        return postQuote;
    }

    public void setPostQuote(String postQuote) {
        this.postQuote = postQuote;
    }

    public boolean isUsingTimeAttribute() {
        return useTimeAttribute;
    }

    public void setUseTimeAttribute(boolean useTimeAttribute) {
        this.useTimeAttribute = useTimeAttribute;
    }

    public String getTimeAttributeName() {
        return timeAttributeName;
    }

    public void setTimeAttributeName(String timeAttributeName) {
        this.timeAttributeName = timeAttributeName;
    }

    public boolean isPostNewToOld() {
        return postNewToOld;
    }

    public void setPostNewToOld(boolean postNewToOld) {
        this.postNewToOld = postNewToOld;
    }

    public void printForumConfig() {
        System.out.println("Configuration for forum " + this.forumId + ": ");
        System.out.println("Url: " + this.url);
        System.out.println("Collection name: " + this.collectionName);
        System.out.println("Board selector: " + this.boardSelector);
        System.out.println("Thread selector: " + this.threadSelector);
        System.out.println("Post selector: " + this.postSelector);
        System.out.println("Next button: " + this.nextButton);
    }
}