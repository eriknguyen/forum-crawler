package CrawlerThread;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Khanh Nguyen on 3/14/2016.
 */
public class ConnectionManager {

    public PoolingHttpClientConnectionManager connectionManager;
    public ConnectionKeepAliveStrategy keepAliveStrategy;
    public CloseableHttpClient client;

    public ConnectionManager() {
        this.connectionManager = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 100
        this.connectionManager.setMaxTotal(100);
        // Increase default max connection per route to 10
        this.connectionManager.setDefaultMaxPerRoute(32);

        this.keepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator
                        (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase
                            ("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 5 * 1000;
            }
        };

        this.client = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(keepAliveStrategy)
                //.setDefaultRequestConfig(RequestConfig.custom().setStaleConnectionCheckEnabled(true).build())
                .build();
    }

    public String getHtmlString(String url) {
        HttpGet get = new HttpGet(url);
        ConnectionThread thread = new ConnectionThread(001, this.client, get, this.connectionManager);
        thread.start();
        System.out.println(this.connectionManager.getTotalStats().toString());
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return thread.getResponseStr();
    }

    public PoolingHttpClientConnectionManager getConnectionManager() {
        return this.connectionManager;
    }

    public CloseableHttpClient getHttpClient() {
        return this.client;
    }


    public static class ConnectionThread extends Thread {
        private CloseableHttpClient client;
        private HttpGet get;
        private int id;
        private PoolingHttpClientConnectionManager connectionManager;
        private String responseStr;

        public ConnectionThread(int id, CloseableHttpClient client, HttpGet get, PoolingHttpClientConnectionManager connectionManager) {
            this.client = client;
            this.get = get;
            this.id = id;
            this.connectionManager = connectionManager;
            this.responseStr = "";
        }

        public String getResponseStr() {
            return this.responseStr;
        }

        @Override
        public void run() {
            try {
                CloseableHttpResponse response = client.execute(get);
                try {
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        responseStr = EntityUtils.toString(response.getEntity());
                        //System.out.println(id + ": " + client.toString() + " done job!");
                        //System.out.println("Thread "+ id + ": " + connectionManager.getTotalStats().toString());
                    }

                } finally {
                    response.close();
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}