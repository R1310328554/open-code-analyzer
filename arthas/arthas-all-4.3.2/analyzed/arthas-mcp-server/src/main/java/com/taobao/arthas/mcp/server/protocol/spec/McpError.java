/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.spec;

import com.taobao.arthas.mcp.server.protocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import com.taobao.arthas.mcp.server.util.Assert;

/**
 * 表示 MCP/JSON-RPC 协议层错误的运行时异常。
 * <p>
 * 封装 {@link JSONRPCError}，便于在处理器中构造标准错误响应；
 * 亦提供 {@link Builder} 与异常链聚合工具方法。
 *
 * @author Yeaury
 */
public class McpError extends RuntimeException {

    /** 对应的 JSON-RPC 错误对象，可为 null（已弃用构造路径）。 */
    private JSONRPCError jsonRpcError;

    /** 以标准 JSON-RPC 错误对象构造异常。 */
    public McpError(JSONRPCError jsonRpcError) {
        super(jsonRpcError.getMessage());
        this.jsonRpcError = jsonRpcError;
    }

    /** @deprecated 请改用 {@link #builder(int)} 或 {@link JSONRPCError} 构造。 */
    @Deprecated
    public McpError(Object error) {
        super(error.toString());
    }

    public JSONRPCError getJsonRpcError() {
        return jsonRpcError;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(super.toString());
        if (jsonRpcError != null) {
            builder.append("\n");
            builder.append(jsonRpcError.toString());
        }
        return builder.toString();
    }

    /** 以 JSON-RPC 错误码启动链式构建。 */
    public static Builder builder(int errorCode) {
        return new Builder(errorCode);
    }

    /** {@link McpError} 的流式构建器。 */
    public static class Builder {

        private final int code;

        private String message;

        private Object data;

        private Builder(int code) {
            this.code = code;
        }

        /** 设置面向客户端的错误描述。 */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /** 附加结构化错误详情（可选）。 */
        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        /** 校验 message 非空后构建 {@link McpError}。 */
        public McpError build() {
            Assert.hasText(message, "message must not be empty");
            return new McpError(new JSONRPCError(code, message, data));
        }

    }

    /** 沿 cause 链向下查找最底层根因，避免循环引用。 */
    public static Throwable findRootCause(Throwable throwable) {
        Assert.notNull(throwable, "throwable must not be null");
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    /** 将异常链各层的类名与 message 拼接为多行摘要，便于写入 error.data。 */
    public static String aggregateExceptionMessages(Throwable throwable) {
        Assert.notNull(throwable, "throwable must not be null");

        StringBuilder messages = new StringBuilder();
        Throwable current = throwable;

        while (current != null) {
            if (messages.length() > 0) {
                messages.append("\n  Caused by: ");
            }

            messages.append(current.getClass().getSimpleName());
            if (current.getMessage() != null) {
                messages.append(": ").append(current.getMessage());
            }

            if (current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }

        return messages.toString();
    }

}
