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
import java.util.List;
import java.util.Random;

/**
 * Created by Khanh Nguyen on 3/25/2016.
 */
public class UpdateDB {

    public static void main(String[] args) throws ParseException, IOException {

        System.out.println(ZonedDateTime.now());
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("fyp");

        MongoCollection sourceCollection = db.getCollection("postsTwo");
        MongoCollection targetCollection = db.getCollection("newPosts");
        FindIterable<Document> postIterable = sourceCollection.find();

        List<InsertOneModel> insertList = new ArrayList<>();

        int count = 0;
        //Document newPost;

        for (Document post :
                postIterable) {
            count++;
            if (count % 10000 == 0) {
                System.out.println("Checked: " + count);
            }
            post.replace("hasQuote", getRandomBoolean());

            insertList.add(new InsertOneModel(post));

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
