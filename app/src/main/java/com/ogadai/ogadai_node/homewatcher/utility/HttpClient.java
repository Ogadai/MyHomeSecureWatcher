package com.ogadai.ogadai_node.homewatcher.utility;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by alee on 05/07/2017.
 */

public class HttpClient {

    private static final String TAG = "HttpClient";

    public static void post(String url, byte[] data) {
        HttpURLConnection client = null;
        try {
            client = connection(url);
            client.setRequestMethod("POST");
            client.setDoOutput(true);
            client.setFixedLengthStreamingMode(data.length);
//            client.setChunkedStreamingMode(0);

            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
            outputPost.write(data);
            outputPost.flush();
            outputPost.close();

        } catch (IOException e) {
            Log.e(TAG, "Error posting binary data", e);
        } finally {
            if (client != null) {
                client.disconnect();
            }
        }
    }

    private static HttpURLConnection connection(String url) throws IOException {
        return (HttpURLConnection) new URL(url).openConnection();
    }
}
