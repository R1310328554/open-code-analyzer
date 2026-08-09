/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;

/**
 * 从服务端原始请求中提取 {@link McpTransportContext} 的函数式接口。
 * <p>
 * 可在 Netty {@code FullHttpRequest} 上读取 Header、URI 等信息并写入上下文。
 *
 * @param <T> 服务端请求类型（通常为 {@code FullHttpRequest}）
 */
@FunctionalInterface
public interface McpTransportContextExtractor<T> {

    /**
     * 从请求中提取传输上下文并填充到 base 实例。
     * 
     * @param serverRequest 待解析的服务端请求
     * @param context 待填充的基础上下文
     * @return 写入提取信息后的上下文
     */
    McpTransportContext extract(T serverRequest, McpTransportContext context);

}
