package Util;

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
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Khanh Nguyen on 3/25/2016.
 */
public class UpdateDB2 {

    public static void main(String[] args) throws ParseException, IOException {

        System.out.println(ZonedDateTime.now());
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("test");

        MongoCollection sourceCollection = db.getCollection("corpus_dup");
        MongoCollection targetCollection = db.getCollection("corpus_duplicate");

        List<Long> idList = new ArrayList<>();
        List<Long> dupList = new ArrayList<>();
        List<InsertOneModel> insertList = new ArrayList<>();
        FindIterable<Document> twitIterable = sourceCollection.find();

        for (Document doc : twitIterable) {
            Long id = doc.getLong("id");
            idList.add(id);
        }

        Collections.sort(idList);
        for (int i = 0; i < idList.size() - 1; i++) {
            if (idList.get(i) == idList.get(i+1)) {
                dupList.add(idList.get(i));
            }
        }

        System.out.println("id list size: " + idList.size());
        System.out.println("dup list size: " + dupList.size());

        for (Long id: dupList) {
            Document doc = new Document("dupId", id);
            insertList.add(new InsertOneModel(doc));
        }

        /*int count = 0;

        for (Document post :
                postIterable) {
            if (post.getString("_id").contains("elComment")) {
                count++;
                if (count % 10000 == 0) {
                    System.out.println("Checked: " + count);
                }
                String boardUrl = post.getString("boardUrl");

                post.append("boardName", boardMap.get(boardUrl)).append("forum", "RenoTalk");

                String threadUrl = post.getString("threadUrl");
                post.append("threadName", threadMap.get(threadUrl));

                insertList.add(new InsertOneModel(post));
            }
        }*/

        System.out.println("Total list size: " + insertList.size());
        //samplePost.bulkWrite(removeList);
        targetCollection.bulkWrite(insertList);
        System.out.println(ZonedDateTime.now());
    }

    public static boolean getRandomBoolean() {
        return Math.random() < 0.4;
    }

}
