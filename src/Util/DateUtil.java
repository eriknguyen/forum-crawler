package Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Khanh Nguyen on 3/4/2016.
 */
public class DateUtil {

    public static Date parseDate(String dateStr, String format) throws ParseException {
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

    public static String formatDate(Date lastPostTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        return formatter.format(lastPostTime);
    }
}
