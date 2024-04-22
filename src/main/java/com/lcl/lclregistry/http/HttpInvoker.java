package com.lcl.lclregistry.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface HttpInvoker {

    Logger log = LoggerFactory.getLogger(HttpInvoker.class);

    HttpInvoker Default = new OkHttpInvoker(500);

    String post(String requestString, String url);

    String get(String url);

    static <T> T httpGet(String url, Class<T> clazz) {
        log.debug(" ======>>>> httpGet url：{}", url);
        String respJson = Default.get(url);
        log.debug(" ======>>>> respJson: {}", respJson);
        return JSON.parseObject(respJson, clazz);
    }


    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        log.debug(" ======>>>> httpGet url：{}", url);
        String respJson = Default.get(url);
        log.debug(" ======>>>> respJson: {}", respJson);
        return JSON.parseObject(respJson, typeReference);
    }

    static <T> T httpPost(String requestString, String url, Class<T> clazz) {
        log.debug(" ======>>>> httpPost url：{}, requestString: {}", url, requestString);
        String respJson = Default.post(requestString, url);
        log.debug(" ======>>>> respJson: {}", respJson);
        return JSON.parseObject(respJson, clazz);
    }
}
