package CrawlerThread;

import Entities.CrawlTask;
import Entities.ForumConfig;
import Entities.ForumPost;
import Entities.ForumThread;
import Util.DateUtil;
import Util.StringUtil;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CrawlerThread extends Thread {

    protected int level;
    protected int id;
    protected TaskQueue queue;
    protected ThreadController tc;
    protected MainCrawler mainCrawler;
    protected ConnectionManager connectionManager;

    public void setConnectionManager(ConnectionManager cm) {
        this.connectionManager = cm;
    }

    public void setId(int _id) {
        id = _id;
    }

    public void setLevel(int _level) {
        level = _level;
    }

    public void setQueue(TaskQueue _queue) {
        queue = _queue;
    }

    public void setThreadController(ThreadController _tc) {
        tc = _tc;
    }

    public void setMessageReceiver(MainCrawler _mr) {
        mainCrawler = _mr;
    }

    public CrawlerThread() {
    }

    public void run() {
        // pop new urls from the queue until queue is empty
        for (CrawlTask newTask = queue.pop(level); newTask != null; newTask = queue.pop(level))
        /*while (queue.getQueueSize(currentLevel)>0)*/ {
//			Object newTask = queue.pop(currentLevel);
            // Tell the message receiver what we're doing now
            mainCrawler.receiveMessage(newTask, id);
            // Process the newTask
            process(newTask);
            // If there are less threads running than it could, try
            // starting more threads
            if (tc.getMaxThreads() > tc.getRunningThreads()) {
                try {
                    tc.startThreads();
                } catch (Exception e) {
                    System.err.println("[" + id + "] " + e.toString());
                }
            }
        }
        // Notify the ThreadController that we're done
        tc.finished(id);
    }

    public void process(CrawlTask crawlTask) {
        // The objects that we're dealing with here a strings for urls

        if (level == 0) {
            try {
                String boardLink = crawlTask.getUrl();
                MongoDatabase db = crawlTask.getDb();
                MongoCollection threadsCollection = db.getCollection("threads");
                ForumConfig forumConfig = crawlTask.getForumConfig();

                List<ForumThread> threadList = new ArrayList<>();
                processBoard(forumConfig, boardLink, threadList, threadsCollection);

                for (ForumThread thread :
                        threadList) {
                    try {
                        String link = thread.getThreadUrl();
                        CrawlTask newTask = new CrawlTask(link, db, forumConfig);
                        queue.push(newTask, level + 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // process of this object has failed, but we just ignore it here
            }
        } else if (level == 1) {
            try {
                String threadLink = crawlTask.getUrl();
                MongoDatabase db = crawlTask.getDb();
                ForumConfig forumConfig = crawlTask.getForumConfig();

				/*List<ForumPost> postList = new ArrayList<>();*/
                processThread(forumConfig, threadLink, db);

            } catch (Exception e) {
                e.printStackTrace();
                // process of this object has failed, but we just ignore it here
            }
        }


    }


    public void processBoard(ForumConfig forum, String boardUrl, List<ForumThread> list, MongoCollection collection) throws IOException {
        String htmlStr = connectionManager.getHtmlString(boardUrl);
        org.jsoup.nodes.Document document;
        int pagesPerBoard = 1;
        //System.out.println("PROCESS BOARD...");

        if (!htmlStr.isEmpty()) {
            document = Jsoup.parse(htmlStr);
            Element lastButton = document.select(forum.getLastButton()).first();
            if (lastButton != null) {
                String lastPageLink = lastButton.absUrl("href");
                pagesPerBoard = StringUtil.extractIndex(lastPageLink, forum.getBoardPageUrlPrefix(), forum.getBoardPageUrlSuffix());
            } else if (forum.isHasLast()) {

            }
        }

        /*traverse throught each pages*/
        /*for (int page = 1; page <= pagesPerBoard; page++) {*/
        for (int page = 1; page <= 3; page++) {
            System.out.println("GET THREAD FROM PAGE " + page + " OF BOARD: " + boardUrl);
            String boardPageUrl = boardUrl + forum.getBoardPageUrlPrefix() + page + forum.getBoardPageUrlSuffix();
            htmlStr = connectionManager.getHtmlString(boardPageUrl);
            if (!htmlStr.isEmpty()) {
                document = Jsoup.parse(htmlStr);
                Elements threadList = document.select(forum.getThreadSelector());
                for (Element threadItem : threadList) {

                    ForumThread thread = new ForumThread();
                    thread.setBoardUrl(boardUrl);
                    Element threadTitle = threadItem.select(forum.getThreadTitle()).first();

                    thread.setThreadUrl(threadTitle.absUrl("href"));
                    thread.setThreadName(threadTitle.text().replaceAll("\'", ""));
                    try {
                        String lastPostTimeStr;
                        if (forum.isUsingTimeAttribute()) {
                            lastPostTimeStr = threadItem.select(forum.getThreadLastPostTime()).first().attr(forum.getTimeAttributeName());
                        } else {
                            lastPostTimeStr = threadItem.select(forum.getThreadLastPostTime()).first().text().replaceAll("\\s", "");
                        }
                        Date lastPostTime;
                        try {
                            lastPostTime = DateUtil.parseStringToDate(lastPostTimeStr, forum.getDateFormat());
                        } catch (Exception e) {
                            lastPostTime = DateUtil.generateRandomThreadLastPostTime();
                        }

                        thread.setLastPostTime(DateUtil.parseDateToString(lastPostTime));
                        thread.setSticky(threadItem.select(forum.getStickyClass()).size() > 0);

                        /*checking thread update using lastPostTime*/
                        /*Document checkThreadId = (Document) collection.find(new Document("_id", thread.getThreadUrl())).first();
                        if (checkThreadId != null) {
                            Date dateFromDB = DateUtil.parseSimpleDate(checkThreadId.getString("threadLastPostTime"));
                            boolean isThreadUpdated = checkThreadId.getBoolean("isThreadUpdated");
                            boolean hasUpdate = dateFromDB.before(lastPostTime);
                            if ((!thread.isSticky()) && (!hasUpdate) && isThreadUpdated) {
                                System.out.println("NO MORE UPDATE FROM THREAD: " + thread.getThreadName());
                                return;
                            }
                        }*/

                        thread.setThreadCreator(threadItem.select(forum.getThreadCreator()).first().text());

                        try {
                            String views = threadItem.select(forum.getThreadViews()).first().text().replaceAll(",", "");
                            thread.setViews(Integer.parseInt(views));
                        } catch (Exception e) {
                            thread.setViews(0);
                        }
                        try {
                            String replies = threadItem.select(forum.getThreadReplies()).first().text().replaceAll(",", "");
                            thread.setReplies(Integer.parseInt(replies));
                        } catch (Exception e) {
                            thread.setReplies(0);
                        }


                        String lastPostUser = threadItem.select(forum.getThreadLastPostUser()).first().text();
                        thread.setLastPostUser(lastPostUser);

                        thread.setUpdated(false);

                        //Add thread to database
                        thread.addThreadToDB(collection);
                        //System.out.println("Thread " + (threadList.indexOf(threadItem)+1) + ": " + thread.getThreadName() + " added to DB");
                        //thread.printThread();
                        list.add(thread);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Thread is moved to other board" + thread.getThreadUrl());
                    }
                }
            }
        }
    }

    public void processThread(ForumConfig forum, String threadUrl, MongoDatabase db) throws IOException, ParseException {
        /*
        * need to iterate through each pages of one thread
        * can use the navigation button to link to next page
        * end when there is no "Last" button
        * */
        MongoCollection collection = db.getCollection("posts");
        String htmlStr = connectionManager.getHtmlString(threadUrl);
        org.jsoup.nodes.Document document;
        String boardUrl = getBoardUrlFromThread(threadUrl, db);

        /*get the number of pages in this thread*/
        int pagesPerThread = 1;
        //System.out.println("PROCESS BOARD...");
        if (!htmlStr.isEmpty()) {
            document = Jsoup.parse(htmlStr);
            Element lastButton = document.select(forum.getLastButton()).first();
            if (lastButton != null) {
                String lastPageLink = lastButton.absUrl("href");
                pagesPerThread = StringUtil.extractIndex(lastPageLink, forum.getThreadPageUrlPrefix(), forum.getThreadPageUrlSuffix());
            }
        }

        /*for each page of a thread*/
        /*for (int page = pagesPerThread; page >=1; page--) {*/
        for (int page = 3; page >=1; page--) {
            System.out.println("GET POST FROM PAGE " + page + " OF THREAD: " + threadUrl);
            String threadPageUrl = threadUrl + forum.getBoardPageUrlPrefix() + page + forum.getBoardPageUrlSuffix();
            htmlStr = connectionManager.getHtmlString(threadPageUrl);

            if (!htmlStr.isEmpty()) {
                document = Jsoup.parse(htmlStr);
                Elements postList = document.select(forum.getPostSelector());

                for (int i = postList.size()-1; i >=0; i--) {
                    Element postElement = postList.get(i);
                    String id = postElement.id();
                    //System.out.println(id.toUpperCase());

                    /*Document checkPostId = (Document) collection.find(new Document("_id", id)).first();
                    if (checkPostId != null) {
                        System.out.println("NO MORE UPDATE FROM POST: " + id);
                        setThreadUpdated(threadUrl, collection);
                        return;
                    }*/

                    ForumPost post = new ForumPost();
                    String url = threadPageUrl + "#" + id;
                    String timeStr;
                    if (forum.isUsingTimeAttribute()) {
                        timeStr = postElement.select(forum.getPostTime()).first().attr(forum.getTimeAttributeName());
                    } else {
                        timeStr = postElement.select(forum.getPostTime()).first().text();
                    }
                    try {
                        timeStr = DateUtil.formatDateString(timeStr, forum.getDateFormat());
                    } catch (Exception e){
                        timeStr = DateUtil.parseDateToString(DateUtil.generateRandomPostTime());
                    }

                    String user = postElement.select(forum.getPostUser()).first().text();
                    //Element postBody = postElement.select()....
                    String content = postElement.select(forum.getPostContent()).first().text();
                    boolean hasQuote = postElement.select(forum.getPostQuote()).size() > 0;

                    post.setPostId(id);
                    post.setPostUrl(url);
                    post.setThreadUrl(threadUrl);
                    post.setBoardUrl(boardUrl);
                    post.setUserName(user);
                    post.setPostContent(content);
                    post.setPostTime(timeStr);
                    post.setHasQuote(hasQuote);

                    post.addPostToDB(collection);
                }


            /*Element nextButton = document.select(forum.getNextButton()).first();
            //System.out.println(nextButton);
            if (nextButton != null) {
                String nextPageUrl = nextButton.select("a").first().absUrl("href");
                //System.out.println("\nGoing to next page..... ");
                processThread(forum, nextPageUrl, collection);
            } else {
                //System.out.println("This is the last page!");
            }*/
            }
        }
        setThreadUpdated(threadUrl, collection);
    }

    private static String getBoardUrlFromThread(String threadUrl, MongoDatabase db) {
        Document threadFromDB = db.getCollection("threads").find(new Document("_id", threadUrl)).first();
        return threadFromDB.getString("boardUrl");
    }

    private static void setThreadUpdated(String threadUrl, MongoCollection collection) {
        collection.updateOne(
                new Document("_id", threadUrl),
                new Document("$set", new Document("isThreadUpdated", true))
        );
    }

}
