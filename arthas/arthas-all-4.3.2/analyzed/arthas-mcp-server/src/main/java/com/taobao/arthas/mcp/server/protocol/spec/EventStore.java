package com.taobao.arthas.mcp.server.protocol.spec;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * JSON-RPC 事件的存储与重播接口。
 * <p>
 * 为 MCP Streamable HTTP 提供事件持久化与 SSE 断线续传能力：
 * 出站/入站消息写入后可按 {@code last-event-id} 从指定位置重播。
 *
 * @author Yeaury
 */
public interface EventStore {

    /** 已持久化的一条 JSON-RPC 事件及其元数据。 */
    class StoredEvent {
        /** SSE 事件标识，用于 Last-Event-ID 续传。 */
        private final String eventId;
        /** 所属 MCP 会话 ID。 */
        private final String sessionId;
        /** 序列化前的 JSON-RPC 消息体。 */
        private final McpSchema.JSONRPCMessage message;
        /** 事件写入时间戳。 */
        private final Instant timestamp;
        
        public StoredEvent(String eventId, String sessionId, McpSchema.JSONRPCMessage message, Instant timestamp) {
            this.eventId = eventId;
            this.sessionId = sessionId;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public String getEventId() {
            return eventId;
        }
        public String getSessionId() {
            return sessionId;
        }
        public McpSchema.JSONRPCMessage getMessage() {
            return message;
        }
        public Instant getTimestamp() {
            return timestamp;
        }
    }

    /** 持久化一条消息并返回分配的事件 ID。 */
    String storeEvent(String sessionId, McpSchema.JSONRPCMessage message);

    /** 从 {@code fromEventId} 之后（不含）开始，按序重播该会话的事件。 */
    Stream<StoredEvent> getEventsForSession(String sessionId, String fromEventId);

    /** 清理超过 {@code maxAge} 毫秒的过期事件。 */
    void cleanupOldEvents(String sessionId, long maxAge);

    /** 删除指定会话的全部事件（会话销毁时调用）。 */
    void removeSessionEvents(String sessionId);

}
