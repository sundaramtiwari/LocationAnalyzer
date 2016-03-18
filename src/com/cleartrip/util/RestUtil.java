package com.cleartrip.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class RestUtil {

    public static String get(String url) {
        String resposneString = "";
        HttpClient client = new DefaultHttpClient();

        try {
            HttpGet request = new HttpGet(url);
            HttpResponse response = null;

            response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";

            while ((line = rd.readLine()) != null) {
                resposneString = resposneString + line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resposneString;
    }
}
