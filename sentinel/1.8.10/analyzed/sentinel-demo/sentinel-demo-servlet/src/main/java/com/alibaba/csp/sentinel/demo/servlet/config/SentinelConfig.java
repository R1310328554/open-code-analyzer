package com.alibaba.csp.sentinel.demo.servlet.config;

import com.alibaba.csp.sentinel.adapter.servlet.callback.DefaultUrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Servlet 演示 Sentinel 配置：注册 URL 限流处理器、来源解析与 URL 清洗器。
 *
 * @author zhangxunwei
 * @date 2024/6/24
 */
public class SentinelConfig implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        initConfig();
    }

    /** 初始化 WebCallbackManager：限流响应、请求来源与 URL 聚合。 */
    public static void initConfig() {
        System.out.println("Init sentinel config");

        WebCallbackManager.setUrlBlockHandler(new DefaultUrlBlockHandler());
        // 从 S-user 请求头解析调用来源
        WebCallbackManager.setRequestOriginParser(request -> request.getHeader("S-user"));
        WebCallbackManager.setUrlCleaner(new MyUrlCleaner());
    }

    /** 将 /foo/数字 聚合为 /foo/* 以便统一限流。 */
    static class MyUrlCleaner implements UrlCleaner {
        @Override
        /** 匹配 /foo/\d+ 时返回 /foo/*。 */
        public String clean(String originUrl) {
            if (originUrl.matches("/foo/\\d+")) {
                return "/foo/*";
            }

            return originUrl;
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }
}
