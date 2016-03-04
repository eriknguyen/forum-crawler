package CrawlerThread;

import Entities.CrawlTask;
import Entities.ForumConfig;
import Entities.ForumPost;
import Entities.ForumThread;
import Util.HtmlHelper;
import com.mongodb.client.MongoCollection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class CrawlerThread extends Thread{

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
	public CrawlerThread() {}

	public void run() {
		// pop new urls from the queue until queue is empty
		for (CrawlTask newTask = queue.pop(level); newTask != null; newTask = queue.pop(level))
		/*while (queue.getQueueSize(currentLevel)>0)*/
		{
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
			System.out.println("GETTING THREADS FROM BOARD");
			try {
				String boardLink = crawlTask.getUrl();
				MongoCollection collection = crawlTask.getCollection();
				ForumConfig forumConfig = crawlTask.getForumConfig();

				List<ForumThread> threadList = new ArrayList<>();
				processBoard(forumConfig, HtmlHelper.getHtmlString(boardLink), threadList, collection);

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
			System.out.println("GETTING POSTS FROM FORUMTHREAD");
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


	public void processBoard(ForumConfig forum, String htmlStr, List<ForumThread> list, MongoCollection collection) {
		//System.out.println("PROCESS BOARD...");
		if (!htmlStr.isEmpty()) {
			org.jsoup.nodes.Document document = Jsoup.parse(htmlStr);

			Elements threadList = document.select(forum.getThreadSelector());

			for (Element threadItem : threadList) {

				ForumThread thread = new ForumThread();
				Element threadTitle = threadItem.select(forum.getThreadTitle()).first();

				thread.setThreadUrl(threadTitle.absUrl("href"));
				thread.setThreadName(threadTitle.text().replaceAll("\'",""));
				try {
					thread.setSticky(threadItem.hasClass(forum.getStickyClass()));
					thread.setThreadCreator(threadItem.select(forum.getThreadCreator()).first().text());

					String replies = threadItem.select(forum.getThreadReplies()).first().text().replaceAll(",","");
					String views = threadItem.select(forum.getThreadViews()).first().text().replaceAll(",","");
					thread.setReplies(Integer.parseInt(replies));
					thread.setViews(Integer.parseInt(views));
					String lastPostTime;
					if (forum.isUsingTimeAttribute()) {
						lastPostTime = threadItem.select(forum.getThreadLastPostTime()).first().attr(forum.getTimeAttributeName());
					} else {
						lastPostTime = threadItem.select(forum.getThreadLastPostTime()).first().text().replaceAll("\\s", "");
					}
					thread.setLastPostTime(lastPostTime);
					String lastPostUser = threadItem.select(forum.getThreadLastPostUser()).first().text();
					thread.setLastPostUser(lastPostUser);

                /*Add thread to database*/
					addThreadToDB(thread, collection);
					//System.out.println("Thread " + (threadList.indexOf(threadItem)+1) + ": " + thread.getThreadName() + " added to DB");
					//thread.printThread();
					list.add(thread);
				} catch (Exception e) {
					System.out.println("Thread is moved to other board" /*+ thread.getThreadUrl()*/);
				}

			}
			Element nextButton = document.select(forum.getNextButton()).first();
			//System.out.println(nextButton);
			if (nextButton != null) {
				String nextPageUrl = nextButton.select("a").first().absUrl("href");
				//System.out.println("\nGoing to next page..... ");
				processBoard(forum, HtmlHelper.getHtmlString(nextPageUrl), list, collection);
			} else {
				//System.out.println("This is the last page!");
			}
		}
	}


	private static void addThreadToDB(ForumThread thread, MongoCollection<org.bson.Document> collection) {
		String threadUrl = thread.getThreadUrl();
		org.bson.Document doc = collection.find(new org.bson.Document("_id", threadUrl)).first();
		if (doc==null) {
			collection.insertOne(
					thread.extractThreadBson()
			);
			//System.out.println("Thread added.");
		} else {
			collection.replaceOne(
					new org.bson.Document("_id", threadUrl),
					thread.extractThreadBson()
			);
			//System.out.println("Thread updated.");
		}
	}

	public static void processThread(ForumConfig forum, String threadUrl, MongoCollection collection) {
		//System.out.println("Processing thread: " + threadUrl);
		String htmlStr = HtmlHelper.getHtmlString(threadUrl);

        /*
        * need to iterate through each pages of one thread
        * can use the navigation button to link to next page
        * end when there is no "Last" button
        * */

        /*for each page of a thread*/
		if (!htmlStr.isEmpty()) {
			ForumPost post = new ForumPost();
			org.jsoup.nodes.Document document = Jsoup.parse(htmlStr);
			Elements postList = document.select(forum.getPostSelector());

			int count = 0;
			for (Element postElement : postList
					) {
				count++;
				String id = postElement.id();
				String url = threadUrl + "#" + id;
				String time;
				if (forum.isUsingTimeAttribute()) {
					time = postElement.select(forum.getPostTime()).first().attr(forum.getTimeAttributeName());
				} else {
					time = postElement.select(forum.getPostTime()).first().text();
				}
				String user = postElement.select(forum.getPostUser()).first().text();
				//Element postBody = postElement.select()....
				String content = postElement.select(forum.getPostContent()).first().text();
				boolean hasQuote = postElement.select(forum.getPostQuote()).size()>0;

				post.setPostId(id);
				post.setPostUrl(url);
				post.setUserName(user);
				post.setPostContent(content);
				post.setPostTime(time);
				post.setHasQuote(hasQuote);
				//post.printPost();
//                System.out.println("POST " + count + ": " + post.getPostId()
//                        + ": " + post.getPostTime()
//                        + " -- " + post.getUserName()
//                        + " -- " + post.getPostUrl());

				addPostToDB(post, collection);
			}


			Element nextButton = document.select(forum.getNextButton()).first();
			//System.out.println(nextButton);
			if (nextButton != null) {
				String nextPageUrl = nextButton.select("a").first().absUrl("href");
				//System.out.println("\nGoing to next page..... ");
				processThread(forum, nextPageUrl, collection);
			} else {
				//System.out.println("This is the last page!");
			}


		}
	}

	private static void addPostToDB(ForumPost post, MongoCollection<org.bson.Document> collection) {
		String postId = post.getPostId();
		org.bson.Document doc = collection.find(new org.bson.Document("_id", postId)).first();
		if (doc==null) {
			collection.insertOne(post.extractPostBson());
		} else {
			collection.replaceOne(
					new org.bson.Document("_id", postId),
					post.extractPostBson()
			);
		}
	}

}
