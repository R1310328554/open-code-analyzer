/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.task;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;

/**
 * Task 执行期间 side-channel 通信的消息类型。
 * <p>
 * {@link QueuedMessage.Request} 与 {@link QueuedMessage.Notification} 由 dequeue 取出并投递给客户端；
 * {@link QueuedMessage.Response} 仅通过 {@code waitForResponse} 按 requestId 匹配获取。
 *
 * @author Yeaury
 */
public abstract class QueuedMessage {

    /** 服务端发往客户端、需客户端回复的请求（如 elicitation、sampling）。 */
    public static class Request extends QueuedMessage {
        private final Object requestId;
        private final String method;
        private final McpSchema.Request request;

        public Request(Object requestId, String method, McpSchema.Request request) {
            this.requestId = requestId;
            this.method = method;
            this.request = request;
        }

        public Object requestId() {
            return requestId;
        }

        public String method() {
            return method;
        }

        public McpSchema.Request request() {
            return request;
        }

        @Override
        public String toString() {
            return "QueuedMessage.Request{requestId=" + requestId + ", method='" + method + "'}";
        }
    }

    /** 客户端对先前 {@link Request} 的响应。 */
    public static class Response extends QueuedMessage {
        private final Object requestId;
        private final McpSchema.Result result;

        public Response(Object requestId, McpSchema.Result result) {
            this.requestId = requestId;
            this.result = result;
        }

        public Object requestId() {
            return requestId;
        }

        public McpSchema.Result result() {
            return result;
        }

        @Override
        public String toString() {
            return "QueuedMessage.Response{requestId=" + requestId + "}";
        }
    }

    /** 异步通知（如进度更新），无需客户端回复。 */
    public static class Notification extends QueuedMessage {
        private final String method;
        private final Object notification;

        public Notification(String method, Object notification) {
            this.method = method;
            this.notification = notification;
        }

        public String method() {
            return method;
        }

        public Object notification() {
            return notification;
        }

        @Override
        public String toString() {
            return "QueuedMessage.Notification{method='" + method + "'}";
        }
    }
}
