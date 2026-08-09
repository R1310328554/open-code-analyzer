package com.alibaba.arthas.nat.agent.common.utils;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 基于 OkHttp 的轻量级 HTTP 工具类，供 Native Agent 集群组件发起同步 GET/POST 请求。
 *
 * @description: OkHttpUtil
 * @author：flzjkl
 * @date: 2024-10-20 21:35
 */
public class OkHttpUtil {

    /** 全局共享的 OkHttp 客户端，统一配置连接/读写超时 */
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /** JSON 请求体的默认 MediaType */
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * 向指定 URL 发起 GET 请求并返回响应体字符串。
     *
     * @param url 目标地址
     */
    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * 向指定 URL 发送 JSON POST 请求并返回响应体字符串。
     *
     * @param url  目标地址
     * @param json JSON 格式的请求体
     */
    public static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

}
