/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taobao.arthas.mcp.server.protocol.server.McpStatelessServerHandler;
import com.taobao.arthas.mcp.server.protocol.server.McpTransportContextExtractor;
import com.taobao.arthas.mcp.server.protocol.server.handler.McpStatelessHttpRequestHandler;
import com.taobao.arthas.mcp.server.protocol.spec.McpStatelessServerTransport;
import com.taobao.arthas.mcp.server.util.Assert;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.concurrent.CompletableFuture;

/**
 * 无状态 MCP 传输的 Netty 适配层，将 {@link McpStatelessServerHandler} 绑定到 HTTP 处理器。
 * <p>
 * 通过 {@link #getMcpRequestHandler()} 暴露底层 {@link McpStatelessHttpRequestHandler} 供 Netty 管道挂载。
 *
 * @see McpStatelessServerTransport
 */
public class NettyStatelessServerTransport implements McpStatelessServerTransport {

    public static final String DEFAULT_MCP_ENDPOINT = "/mcp";
    
    private final McpStatelessHttpRequestHandler requestHandler;

    /**
     * 私有构造：创建内部 {@link McpStatelessHttpRequestHandler}。
     * 
     * @param objectMapper JSON 序列化器
     * @param mcpEndpoint MCP HTTP 端点路径
     * @param contextExtractor 传输上下文提取器
     * @throws IllegalArgumentException 任一参数为 null
     */
    private NettyStatelessServerTransport(ObjectMapper objectMapper, String mcpEndpoint,
                                          McpTransportContextExtractor<FullHttpRequest> contextExtractor) {
        Assert.notNull(objectMapper, "ObjectMapper must not be null");
        Assert.notNull(mcpEndpoint, "MCP endpoint must not be null");
        Assert.notNull(contextExtractor, "Context extractor must not be null");

        this.requestHandler = new McpStatelessHttpRequestHandler(objectMapper, mcpEndpoint, contextExtractor);
    }

    @Override
    public void setMcpHandler(McpStatelessServerHandler mcpHandler) {
        requestHandler.setMcpHandler(mcpHandler);
    }

    /**
     * Initiates a graceful shutdown of the transport.
     * 
     * @return A CompletableFuture that completes when all cleanup operations are finished
     */
    @Override
    public CompletableFuture<Void> closeGracefully() {
        return requestHandler.closeGracefully();
    }

    /**
     * Gets the underlying HTTP request handler.
     * 
     * @return The McpStatelessHttpRequestHandler instance
     */
    public McpStatelessHttpRequestHandler getMcpRequestHandler() {
        if (this.requestHandler != null) {
            return this.requestHandler;
        }
        throw new UnsupportedOperationException("Stateless transport provider does not support request handler");
    }

    public static Builder builder() {
        return new Builder();
    }

    /** {@link NettyStatelessServerTransport} 的流式构建器。 */

    public static class Builder {

        private ObjectMapper objectMapper;
        private String mcpEndpoint = DEFAULT_MCP_ENDPOINT;
        private McpTransportContextExtractor<FullHttpRequest> contextExtractor = (serverRequest, context) -> context;

        public Builder objectMapper(ObjectMapper objectMapper) {
            Assert.notNull(objectMapper, "ObjectMapper must not be null");
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder mcpEndpoint(String mcpEndpoint) {
            Assert.notNull(mcpEndpoint, "MCP endpoint must not be null");
            this.mcpEndpoint = mcpEndpoint;
            return this;
        }

        public Builder contextExtractor(McpTransportContextExtractor<FullHttpRequest> contextExtractor) {
            Assert.notNull(contextExtractor, "Context extractor must not be null");
            this.contextExtractor = contextExtractor;
            return this;
        }

        public NettyStatelessServerTransport build() {
            Assert.notNull(this.objectMapper, "ObjectMapper must be set");
            Assert.notNull(this.mcpEndpoint, "MCP endpoint must be set");

            return new NettyStatelessServerTransport(this.objectMapper, this.mcpEndpoint, this.contextExtractor);
        }
    }
}
