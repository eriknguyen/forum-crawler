package Util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Created by Khanh Nguyen on 3/3/2016.
 */
public class HtmlHelper {

    /*
    * extract the HTML page as a string from a root URL string
    *
    * */
    public static String getHtmlString(String rootUrl) {

        /*HttpRequestRetryHandler retryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException e, int i, HttpContext httpContext) {
                if (i >= 5) {
                    // Do not retry if over max retry count
                    return false;
                }
                *//*if (e instanceof InterruptedIOException) {
                    // timeout
                    return false;
                }
                if ( e instanceof UnknownHostException) {
                    // unknown host
                    return false;
                }
                if (e instanceof ConnectTimeoutException) {
                    // Connection refused
                    return false;
                }
                if (e instanceof SSLException) {
                    // SSL handshake exception
                    return false;
                }*//*
                HttpClientContext clientContext = HttpClientContext.adapt(httpContext);
                HttpRequest request = clientContext.getRequest();
                boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                if (idempotent) {
                    // retry if the request is considered idempotent
                    return true;
                }
                return false;
            }
        };*/

        //HttpClient httpClient = HttpClients.custom().setRetryHandler(retryHandler).build();
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
            e.printStackTrace();
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
