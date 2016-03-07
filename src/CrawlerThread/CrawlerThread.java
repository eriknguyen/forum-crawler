package CrawlerThread;

import Entities.CrawlTask;
import Entities.ForumConfig;
import Entities.ForumPost;
import Entities.ForumThread;
import Util.DateUtil;
import Util.HtmlHelper;
import Util.StringUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CrawlerThread extends Thread {

    protected int level;
    protected int id;
    protected TaskQueue queue;
    protected ThreadController tc;
    protected MainCrawler mr;

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
        mr = _mr;
    }

    public CrawlerThread() {
    }

    public void run() {
        // pop new urls from the queue until queue is empty
        for (CrawlTask newTask = queue.pop(level); newTask != null; newTask = queue.pop(level))
        /*while (queue.getQueueSize(currentLevel)>0)*/ {
//			Object newTask = queue.pop(currentLevel);
            // Tell the message receiver what we're doing now
            mr.receiveMessage(newTask, id);
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
                MongoCollection collection = crawlTask.getCollection();
                ForumConfig forumConfig = crawlTask.getForumConfig();

                List<ForumThread> threadList = new ArrayList<>();
                processBoard(forumConfig, boardLink, threadList, collection);

                for (ForumThread thread :
                        threadList) {
                    try {
                        String link = thread.getThreadUrl();
                        CrawlTask newTask = new CrawlTask(link, crawlTask.getCollection(), forumConfig);
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
                MongoCollection collection = crawlTask.getCollection();
                ForumConfig forumConfig = crawlTask.getForumConfig();

				/*List<ForumPost> postList = new ArrayList<>();*/
                processThread(forumConfig, threadLink, collection);

            } catch (Exception e) {
                e.printStackTrace();
                // process of this object has failed, but we just ignore it here
            }
        }


    }


    public void processBoard(ForumConfig forum, String boardUrl, List<ForumThread> list, MongoCollection collection) {
        String htmlStr = HtmlHelper.getHtmlString(boardUrl);
        org.jsoup.nodes.Document document;
        int pagesPerBoard = 1;
        //System.out.println("PROCESS BOARD...");
        if (!htmlStr.isEmpty()) {
            document = Jsoup.parse(htmlStr);
            Element lastButton = document.select(forum.getLastButton()).first();
            if (lastButton != null) {
                String lastPageLink = lastButton.absUrl("href");
                pagesPerBoard = StringUtil.extractIndex(lastPageLink, forum.getBoardPageUrlPrefix(), forum.getBoardPageUrlSuffix());
            }
        }

        for (int page = 1; page <= pagesPerBoard; page++) {
            System.out.println("GET THREAD FROM PAGE " + page + " OF BOARD: " + boardUrl);
            String boardPageUrl = boardUrl + forum.getBoardPageUrlPrefix() + page + forum.getBoardPageUrlSuffix();
            htmlStr = HtmlHelper.getHtmlString(boardPageUrl);
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
                        Date lastPostTime = DateUtil.parseDate(lastPostTimeStr, forum.getDateFormat());
                        thread.setLastPostTime(DateUtil.formatDate(lastPostTime));
                        thread.setSticky(threadItem.select(forum.getStickyClass()).size() > 0);

                        /*Document checkThreadId = (Document) collection.find(new Document("_id", thread.getThreadUrl())).first();
                        if (checkThreadId != null) {
                            Date dateFromDB = DateUtil.parseSimpleDate(checkThreadId.getString("threadLastPostTime"));
                            boolean hasUpdate = dateFromDB.before(lastPostTime);
                            if ((!thread.isSticky()) && (!hasUpdate)) {
                                System.out.println("NO MORE UPDATE FROM THREAD: " + thread.getThreadName());
                                return;
                            }
                        }*/

                        thread.setThreadCreator(threadItem.select(forum.getThreadCreator()).first().text());
                        String replies = threadItem.select(forum.getThreadReplies()).first().text().replaceAll(",", "");
                        String views = threadItem.select(forum.getThreadViews()).first().text().replaceAll(",", "");
                        thread.setReplies(Integer.parseInt(replies));
                        thread.setViews(Integer.parseInt(views));

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

    public static void processThread(ForumConfig forum, String threadUrl, MongoCollection collection) {
        /*
        * need to iterate through each pages of one thread
        * can use the navigation button to link to next page
        * end when there is no "Last" button
        * */

        String htmlStr = HtmlHelper.getHtmlString(threadUrl);
        org.jsoup.nodes.Document document;
        String boardUrl = getBoardUrlFromThread(threadUrl, collection);

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
        for (int page = pagesPerThread; page >=1; page--) {
            System.out.println("GET POST FROM PAGE " + page + " OF THREAD: " + threadUrl);
            String threadPageUrl = threadUrl + forum.getBoardPageUrlPrefix() + page + forum.getBoardPageUrlSuffix();
            htmlStr = HtmlHelper.getHtmlString(threadPageUrl);

            if (!htmlStr.isEmpty()) {
                document = Jsoup.parse(htmlStr);
                Elements postList = document.select(forum.getPostSelector());

                for (int i = postList.size()-1; i >=0; i--) {
                    Element postElement = postList.get(i);
                    String id = postElement.id();

                    /*Document checkPostId = (Document) collection.find(new Document("_id", id)).first();
                    if (checkPostId != null) {
                        System.out.println("NO MORE UPDATE FROM POST: " + id);
                        setThreadUpdated(threadUrl, collection);
                        return;
                    }*/

                    ForumPost post = new ForumPost();
                    String url = threadPageUrl + "#" + id;
                    String time;
                    if (forum.isUsingTimeAttribute()) {
                        time = postElement.select(forum.getPostTime()).first().attr(forum.getTimeAttributeName());
                    } else {
                        time = postElement.select(forum.getPostTime()).first().text();
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
                    post.setPostTime(time);
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

    private static String getBoardUrlFromThread(String threadUrl, MongoCollection collection) {
        Document threadFromDB = (Document) collection.find(new Document("_id", threadUrl)).first();
        return threadFromDB.getString("boardUrl");
    }

    private static void setThreadUpdated(String threadUrl, MongoCollection collection) {
        collection.updateOne(
                new Document("_id", threadUrl),
                new Document("$set", new Document("isThreadUpdated", true))
        );
    }

}
