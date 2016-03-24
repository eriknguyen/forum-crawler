package Util;

import CrawlerThread.ConnectionManager;
import Entities.ForumConfig;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import org.bson.Document;

import java.io.IOException;
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        Date date = formatter.parse(dateStr);
        return date;
    }

    public static String parseDateToString(Date lastPostTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
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

    public static void main(String[] args) throws ParseException, IOException {

        System.out.println(ZonedDateTime.now());
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase db = mongoClient.getDatabase("fyp");

        MongoCollection samplePost = db.getCollection("renotalk");
        FindIterable<Document> postIterable = samplePost.find(new Document("postUrl", new Document("$exists", true)));

        List<UpdateOneModel> updateList = new ArrayList<>();
        //BulkWriteOptions bulkWriteOptions = new BulkWriteOptions();
        int formatted = 0;
        int notFormatted = 0;
        for (Document post :
                postIterable) {
            String postTime = post.getString("postTime");
            String formattedTime;
            try {
                formattedTime = convertDateToSolrFormat(postTime);
                notFormatted++;
            } catch (ParseException e) {
                //System.err.println(e);
                //formattedTime = postTime;
                formatted++;
                continue;
            }
            /*samplePost.updateOne(
                    new Document("postTime", postTime),
                    new Document("$set", new Document("postTime", formattedTime))
            );*/
            UpdateOneModel updateOneModel = new UpdateOneModel(
                    new Document("postTime", postTime),
                    new Document("$set", new Document("postTime", formattedTime))
            );
            updateList.add(updateOneModel);
            if (updateList.size() == 1000) {
                break;
            }
            //System.out.println("Current update list size: " + updateList.size() + " | Update model: " + updateOneModel);
        }
        System.out.println("Formatted: " + formatted);
        System.out.println("not yet: " + notFormatted);
        System.out.println("total: " + (formatted+notFormatted) + "? " + "632642");

        System.out.println("Total list size: " + updateList.size());
        samplePost.bulkWrite(updateList);
        System.out.println(ZonedDateTime.now());
    }
}
