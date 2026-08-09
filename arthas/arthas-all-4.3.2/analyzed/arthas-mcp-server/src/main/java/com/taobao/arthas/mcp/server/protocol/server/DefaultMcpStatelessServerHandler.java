/*
 * Copyright 2024-2024 the original author or authors.
 */

package com.taobao.arthas.mcp.server.protocol.server;

import com.taobao.arthas.mcp.server.CommandExecutor;
import com.taobao.arthas.mcp.server.protocol.spec.McpError;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.session.ArthasCommandContext;
import com.taobao.arthas.mcp.server.session.ArthasCommandSessionManager;
import com.taobao.arthas.mcp.server.util.McpAuthExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * 无状态 MCP 请求处理器：为每个 JSON-RPC 请求创建临时 Arthas session，执行完毕后立即关闭。
 */
class DefaultMcpStatelessServerHandler implements McpStatelessServerHandler {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMcpStatelessServerHandler.class);

	Map<String, McpStatelessRequestHandler<?>> requestHandlers;

	Map<String, McpStatelessNotificationHandler> notificationHandlers;

	private final CommandExecutor commandExecutor;

	private final ArthasCommandSessionManager commandSessionManager;

	public DefaultMcpStatelessServerHandler(Map<String, McpStatelessRequestHandler<?>> requestHandlers,
                                            Map<String, McpStatelessNotificationHandler> notificationHandlers,
                                            CommandExecutor commandExecutor) {
		this.requestHandlers = requestHandlers;
		this.notificationHandlers = notificationHandlers;
		this.commandExecutor = commandExecutor;
		this.commandSessionManager = new ArthasCommandSessionManager(commandExecutor);
	}

	@Override
	public CompletableFuture<McpSchema.JSONRPCResponse> handleRequest(McpTransportContext ctx, McpSchema.JSONRPCRequest req) {
		// 为本次请求创建临时 session，无状态模式下不跨请求复用
		String tempSessionId = UUID.randomUUID().toString();
		ArthasCommandSessionManager.CommandSessionBinding binding = commandSessionManager.createCommandSession(tempSessionId);
		ArthasCommandContext commandContext = new ArthasCommandContext(commandExecutor, binding);

		// 从传输上下文提取认证主体并绑定到 session
		Object authSubject = ctx.get(McpAuthExtractor.MCP_AUTH_SUBJECT_KEY);
		if (authSubject != null) {
			commandExecutor.setSessionAuth(binding.getArthasSessionId(), authSubject);
			logger.debug("Applied auth subject to stateless session: {}", binding.getArthasSessionId());
		}

		// 从传输上下文提取 userId，用于统计上报
		String userId = (String) ctx.get(McpAuthExtractor.MCP_USER_ID_KEY);
		if (userId != null) {
			commandExecutor.setSessionUserId(binding.getArthasSessionId(), userId);
			logger.debug("Applied userId to stateless session: {}", binding.getArthasSessionId());
		}

		McpStatelessRequestHandler<?> handler = requestHandlers.get(req.getMethod());
		if (handler == null) {
			// 找不到对应 method 的 handler 时清理临时 session
			closeSession(binding);
			CompletableFuture<McpSchema.JSONRPCResponse> f = new CompletableFuture<>();
			f.completeExceptionally(new McpError("Missing handler for request type: " + req.getMethod()));
			return f;
		}
		try {
			@SuppressWarnings("unchecked")
			CompletableFuture<Object> result = (CompletableFuture<Object>) handler
					.handle(ctx, commandContext, req.getParams());
			return result.handle((r, ex) -> {
				// 请求处理完成后关闭临时 session，避免泄漏
				closeSession(binding);

				if (ex != null) {
					Throwable cause = ex instanceof CompletionException ? ex.getCause() : ex;
					return new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, req.getId(), null,
							new McpSchema.JSONRPCResponse.JSONRPCError(McpSchema.ErrorCodes.INTERNAL_ERROR, cause.getMessage(), null));
				}
				return new McpSchema.JSONRPCResponse(McpSchema.JSONRPC_VERSION, req.getId(), r, null);
			});
		} catch (Throwable t) {
			// Clean up session on error
			closeSession(binding);

			CompletableFuture<McpSchema.JSONRPCResponse> f = new CompletableFuture<>();
			f.completeExceptionally(t);
			return f;
		}
	}

	private void closeSession(ArthasCommandSessionManager.CommandSessionBinding binding) {
		try {
			commandExecutor.closeSession(binding.getArthasSessionId());
		} catch (Exception e) {
			logger.warn("Failed to close temporary session: {}", binding.getArthasSessionId(), e);
		}
	}

	@Override
	public CompletableFuture<Void> handleNotification(McpTransportContext ctx,
													  McpSchema.JSONRPCNotification note) {
		McpStatelessNotificationHandler handler = notificationHandlers.get(note.getMethod());
		if (handler == null) {
			logger.warn("Missing handler for notification: {}", note.getMethod());
			return CompletableFuture.completedFuture(null);
		}
		try {
			return handler.handle(ctx, note.getParams());
		} catch (Throwable t) {
			CompletableFuture<Void> f = new CompletableFuture<>();
			f.completeExceptionally(t);
			return f;
		}
	}

}
