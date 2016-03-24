package CrawlerThread;

import Entities.CrawlTask;
import Entities.ForumConfig;
import Util.HtmlHelper;
import Util.StringUtil;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

public class MainCrawler {

	/*Declare some const*/
	public static final String ID_HARDWAREZONE = "hardwarezone";
	public static final String SG_EXPATS = "http://forum.singaporeexpats.com";
	public static final String VR_ZONE = "http://forums.vr-zone.com";
	public static final String ID_CLUBSNAP = "clubsnap";
	public static final String ID_RENOTALK = "renotalk";
	public static final String KIASUPARENTS = "http://www.kiasuparents.com/kiasu/forum";
	public static final String BRIGHTSPARKS = "http://forum.brightsparks.com.sg";
	public static final String SGCLUB = "http://forums.sgclub.com";
	public static final String MYCARFORUM = "http://www.mycarforum.com/index";
	public static final String SGFORUMS = "http://www.sgforums.com";
	public static final String SGBRIDES = "http://singaporebrides.com/weddingforum";
	public static final String COZYCOT = "http://forums.cozycot.com/forum";
	public static final String SALARY = "http://forums.salary.sg";
	public static final String TOWKAYZONE = "http://www.towkayzone.com.sg";

	/*Constructor for MainCrawler*/
	public MainCrawler(ConnectionManager connectionManager, TaskQueue q, int maxLevel, int maxThreads)
		throws InstantiationException, IllegalAccessException {
		ThreadController tc = new ThreadController(CrawlerThread.class,
												   connectionManager,
												   maxThreads,
												   maxLevel,
												   q,
												   0,
												   this);
	}

	public void finishedAll() {
		// ignore
		System.out.println("Finished at: " + ZonedDateTime.now());

	}

	public void receiveMessage(CrawlTask task, int threadId) {
		// In our case, the object is already string, but that doesn't matter
		System.out.println("[" + threadId + "] " + task.getUrl());
	}

	public void finished(int threadId) {
		System.out.println("[" + threadId + "] finished");
	}

	public static void main(String[] args) throws ParseException, IOException {

		ConnectionManager connectionManager = new ConnectionManager();

		System.out.println(ZonedDateTime.now());
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase db = mongoClient.getDatabase("fyp");

		/*Get the forum configuration from DB, forumConfig collection*/
		Hashtable<String, ForumConfig> forumTable = new Hashtable<>();
		MongoCollection configCollection = db.getCollection("forumConfig");
		FindIterable<Document> configIterable = configCollection.find();
		for (org.bson.Document doc : configIterable) {
			try {
				ForumConfig forum = new ForumConfig(doc);
				forumTable.put(forum.getForumId(), forum);
			} catch (NullPointerException e) {
				//do something
				System.out.println("EXCEPTION IN: getting forumConfig");
				e.printStackTrace();
			}
		}

		ForumConfig forumConfig = forumTable.get(ID_RENOTALK);

		/*
        * Check if there is any new board. No need to run very often
        *
        * */
		MongoCollection collection = db.getCollection(forumConfig.getCollectionName());
		checkBoardUpdate(connectionManager, forumConfig, collection);

		List<String> boardList = new ArrayList<>();
		FindIterable<org.bson.Document> boardIterable = collection.find(new org.bson.Document("boardName", new org.bson.Document("$exists", true)));
		for (org.bson.Document doc : boardIterable ) {
			boardList.add(doc.getString("_id"));
		}

		for (String board :
				boardList) {
			System.out.println(boardList.indexOf(board) + ": " + board);
		}

		String prefix = "testPrefix_";
		int maxLevel = 1;
		int maxThreads = 32;

		try {
			TaskQueue queue = new TaskQueue();
			queue.setFilenamePrefix(prefix);

			for (int i = 0; i < boardList.size(); i++) {
				String url = boardList.get(i);
				System.out.println(i + ": " + url);
				CrawlTask task = new CrawlTask(url, collection, forumConfig);
				queue.push(task, 0);
			}

			new MainCrawler(connectionManager, queue, maxLevel, maxThreads);
			//return;

		} catch (Exception e) {
			System.err.println("An error occured: ");
			e.printStackTrace();
		}
	}



	/*
    * method to check if there is any new board added to the forum. Rarely check just to ensure the information is enough
    *
    * */
	public static void checkBoardUpdate(ConnectionManager connectionManager, ForumConfig forum, MongoCollection<org.bson.Document> collection) throws IOException {
		org.jsoup.nodes.Document document;
		String htmlStr = "";

        /*if the board links haven't been saved to DB, get from index and save those links to DB*/
		htmlStr = connectionManager.getHtmlString(forum.getUrl());
		//htmlStr = connectionManager.getHtmlString("http://www.rentalk.com/foru/");

		if (!htmlStr.isEmpty()) {   //check if the htmlStr parsed is valid

            /*Parse htmlString to Jsoup document object*/
			document = Jsoup.parse(htmlStr);
			document.setBaseUri(forum.getUrl());

            /*extract the needed links from Jsoup document*/
			Elements groupLinks = document.select(forum.getBoardSelector());
			for (Element link : groupLinks) {
				String url = link.absUrl("href");
				org.bson.Document doc = collection.find(new org.bson.Document("_id", url)).first();
				if (doc==null) {
					collection.insertOne(
							new org.bson.Document("boardName", link.text().replaceAll("'", ""))
									.append("_id", url)
					);
				}
			}
		} else {
			System.out.println("Invalid html string");
		}
	}





}
