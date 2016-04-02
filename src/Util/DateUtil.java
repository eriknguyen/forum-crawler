package Util;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import org.bson.Document;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Created by Khanh Nguyen on 3/4/2016.
 */
public class DateUtil {

    public static Date parseStringToDate(String dateStr, String format) throws ParseException {
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        date = formatter.parse(dateStr);
        return date;
    }

    public static Date parseSimpleDate(String dateStr) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = formatter.parse(dateStr);
        return date;
    }

    public static String parseDateToString(Date lastPostTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return formatter.format(lastPostTime);
    }

    public static String formatDateString(String dateStr, String format) throws ParseException {
        Date date = DateUtil.parseStringToDate(dateStr, format);
        return DateUtil.parseDateToString(date);
    }

    public static String convertDateToSolrFormat(String dateStr) throws ParseException {
        Date date = parseSimpleDate(dateStr);
        SimpleDateFormat sorlDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String solrDate = sorlDateFormatter.format(date);
        return solrDate;
    }

    public static Date generateRandomThreadLastPostTime() {
        long offset = Timestamp.valueOf("2016-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2016-03-25 00:00:00").getTime();
        long diff = end - offset + 1;
        Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
        Date date = new Date(rand.getTime());
        return date;
    }

    public static Date generateRandomPostTime() {
        long offset = Timestamp.valueOf("2015-01-01 00:00:00").getTime();
        long end = Timestamp.valueOf("2016-03-25 00:00:00").getTime();
        long diff = end - offset + 1;
        Timestamp rand = new Timestamp(offset + (long)(Math.random() * diff));
        Date date = new Date(rand.getTime());
        return date;
    }

    public static void main(String[] args) throws ParseException, IOException {

        System.out.println(ZonedDateTime.now());
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("test");

        MongoCollection sourceCollection = db.getCollection("posts");
        MongoCollection targetCollection = db.getCollection("newposts");
        FindIterable<Document> postIterable = sourceCollection.find();

        List<DeleteOneModel> removeList = new ArrayList<>();
        List<InsertOneModel> insertList = new ArrayList<>();
        int count = 0;
        int formatted = 0;
        int notFormatted = 0;
        Document newPost;
        for (Document post :
                postIterable) {
            count++;
            if (count % 10000 == 0) {
                System.out.println("Checked: " + count);
            }
            String postTime = post.getString("threadLastPostTime");
            String formattedTime;
            try {
                formattedTime = convertDateToSolrFormat(postTime);
                notFormatted++;
            } catch (ParseException e) {
                //System.err.println(e);
                //formattedTime = postTime;
                formatted++;
                removeList.add(new DeleteOneModel(
                        new Document("postTime", postTime)
                ));
                //insertList.add(new InsertOneModel(post));
                continue;
            }

            newPost = post;
            newPost.replace("threadLastPostTime", formattedTime);
            //System.out.println(post);
            //System.out.println(newPost);
            insertList.add(new InsertOneModel(newPost));

        }
        System.out.println("Formatted: " + formatted);
        System.out.println("not yet: " + notFormatted);
        System.out.println("total: " + (formatted+notFormatted) + "? " + "632642");

        System.out.println("Total list size: " + insertList.size());
        //samplePost.bulkWrite(removeList);
        targetCollection.bulkWrite(insertList);
        System.out.println(ZonedDateTime.now());
    }
}
