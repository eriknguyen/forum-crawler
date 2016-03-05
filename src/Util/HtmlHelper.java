package Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

/**
 * Created by Khanh Nguyen on 3/3/2016.
 */
public class HtmlHelper {

    /*
    * extract the HTML page as a string from a root URL string
    *
    * */
    public static String getHtmlString(String rootUrl) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet();
        String htmlStr = "";
        try {
            httpGet.setURI(URI.create(rootUrl));
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                htmlStr = fetchHtmlString(entity);
            }

        } catch (IOException e) {
            System.err.println(e);
        }
        return htmlStr;
    }

    /*
    * convert HttpEntity content to string for easier processing
    *
    * */
    public static String fetchHtmlString(HttpEntity entity) throws IOException {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(entity.getContent())
        );

        String line;
        StringBuffer stringBuffer = new StringBuffer();
        while ((line = reader.readLine()) != null ) {
            stringBuffer.append(line);
        }
        return stringBuffer.toString();
    }

}
