package Util;

import Entities.ForumConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import org.bson.Document;

import java.io.IOException;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

/**
 * Created by Khanh Nguyen on 3/25/2016.
 */
public class UpdateDB {

    public static void main(String[] args) throws ParseException, IOException {

        System.out.println(ZonedDateTime.now());
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("test");

        MongoCollection sourceCollection = db.getCollection("posts");
        MongoCollection targetCollection = db.getCollection("newposts");
        FindIterable<Document> postIterable = sourceCollection.find();

        List<InsertOneModel> insertList = new ArrayList<>();

        Hashtable<String, String> boardMap = new Hashtable<>();

        FindIterable<Document> boards = db.getCollection("boards").find();
        for (org.bson.Document doc : boards) {
            boardMap.put(doc.getString("_id"), doc.getString("boardName"));
        }

        Hashtable<String, String> threadMap = new Hashtable<>();

        FindIterable<Document> threads = db.getCollection("threads").find();
        for (org.bson.Document doc : threads) {
            threadMap.put(doc.getString("_id"), doc.getString("threadName"));
        }

        int count = 0;

        for (Document post :
                postIterable) {

            if (post.getString("_id").contains("elComment")) {
                count++;
                if (count % 10000 == 0) {
                    System.out.println("Checked: " + count);
                }
                String boardUrl = post.getString("boardUrl");
                /*String forum;
                if (boardUrl.contains("renotalk")) {
                    forum = "RenoTalk";
                } else {
                    forum = "VR-Zone";
                }*/
                post.append("boardName", boardMap.get(boardUrl)).append("forum", "RenoTalk");

                /*String postContent = post.getString("postContent");
                if (postContent.contains("<a rel=")) {
                    postContent = postContent.substring(postContent.lastIndexOf("</a>")+4);
                }
                post.replace("postContent", postContent);*/

                String threadUrl = post.getString("threadUrl");
                post.append("threadName", threadMap.get(threadUrl));

                insertList.add(new InsertOneModel(post));
            }

        }

        /*System.out.println("Formatted: " + formatted);
        System.out.println("not yet: " + notFormatted);
        System.out.println("total: " + (formatted+notFormatted) + "? " + "632642");*/

        System.out.println("Total list size: " + insertList.size());
        //samplePost.bulkWrite(removeList);
        targetCollection.bulkWrite(insertList);
        System.out.println(ZonedDateTime.now());
    }

    public static boolean getRandomBoolean() {
        return Math.random() < 0.4;
    }

}
