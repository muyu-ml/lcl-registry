package com.lcl.lclregistry.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpInvoker implements HttpInvoker {

    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client;

    public OkHttpInvoker(int timeout){
        this.client = new OkHttpClient.Builder()
                .connectionPool(new okhttp3.ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public String post(String requestString, String url) {
        log.debug(" ======>>>> post url：{}, requestString: {}", url, requestString);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestString, JSONTYPE))
                .build();
        try {
            String respJson = this.client.newCall(request).execute().body().string();
            log.debug("respJson: {}", respJson);
            return respJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String get(String url) {
        log.debug(" ======>>>> get url：{}", url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        try {
            String respJson = this.client.newCall(request).execute().body().string();
            log.debug("respJson: {}", respJson);
            return respJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
