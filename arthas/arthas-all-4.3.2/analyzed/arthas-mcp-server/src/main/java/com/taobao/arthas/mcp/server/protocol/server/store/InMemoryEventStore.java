package com.taobao.arthas.mcp.server.protocol.server.store;

import com.taobao.arthas.mcp.server.protocol.spec.EventStore;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link EventStore} 的内存实现，按会话 ID 存储 SSE 事件以支持断线重放。
 * <p>
 * 每个会话保留有限条数并在超限时淘汰最旧事件，防止内存泄漏。
 *
 * @author Yeaury
 */
public class InMemoryEventStore implements EventStore {
    
    private static final Logger logger = LoggerFactory.getLogger(InMemoryEventStore.class);
    
    /** 全局递增的事件 ID 计数器 */
    private final AtomicLong globalEventIdCounter = new AtomicLong(0);
    
    /** 事件存储：sessionId -> 按时间顺序排列的事件列表 */
    private final Map<String, List<StoredEvent>> sessionEvents = new ConcurrentHashMap<>();
    
    /**
     * Event ID to session mapping for fast lookup
     */
    private final Map<String, String> eventIdToSession = new ConcurrentHashMap<>();
    
    /**
     * Maximum events to keep per session (prevent memory leaks)
     */
    private final int maxEventsPerSession;
    
    /**
     * Default retention time in milliseconds (24 hours)
     */
    private final long defaultRetentionMs;
    
    public InMemoryEventStore() {
        this(1000, 24 * 60 * 60 * 1000L); // 1000 events, 24 hours
    }
    
    public InMemoryEventStore(int maxEventsPerSession, long defaultRetentionMs) {
        this.maxEventsPerSession = maxEventsPerSession;
        this.defaultRetentionMs = defaultRetentionMs;
    }
    
    @Override
    public String storeEvent(String sessionId, McpSchema.JSONRPCMessage message) {
        String eventId = String.valueOf(globalEventIdCounter.incrementAndGet());
        Instant timestamp = Instant.now();
        
        StoredEvent event = new StoredEvent(eventId, sessionId, message, timestamp);
        
        sessionEvents.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(event);
        eventIdToSession.put(eventId, sessionId);
        
        // 超出单会话上限时移除最旧事件
        List<StoredEvent> events = sessionEvents.get(sessionId);
        if (events.size() > maxEventsPerSession) {
            // 从列表头部删除最旧事件并同步 eventId 索引
            int toRemove = events.size() - maxEventsPerSession;
            for (int i = 0; i < toRemove; i++) {
                StoredEvent removedEvent = events.remove(0);
                eventIdToSession.remove(removedEvent.getEventId());
            }
            logger.debug("Cleaned up {} old events for session {}", toRemove, sessionId);
        }
        
        logger.trace("Stored event {} for session {}", eventId, sessionId);
        return eventId;
    }
    
    @Override
    public Stream<StoredEvent> getEventsForSession(String sessionId, String fromEventId) {
        List<StoredEvent> events = sessionEvents.get(sessionId);
        if (events == null || events.isEmpty()) {
            return Stream.empty();
        }
        
        if (fromEventId == null) {
            return Stream.empty();
        }
        
        boolean foundStartEvent = false;
        List<StoredEvent> result = new ArrayList<>();
        
        for (StoredEvent event : events) {
            if (!foundStartEvent) {
                if (event.getEventId().equals(fromEventId)) {
                    foundStartEvent = true;
                    result.add(event);
                    // 重放完成后从存储中清除已投递事件
                    events.remove(event);
                    eventIdToSession.remove(event.getEventId());
                }
                continue;
            }
            result.add(event);
        }
        
        return result.stream();
    }

    @Override
    public void cleanupOldEvents(String sessionId, long maxAge) {
        List<StoredEvent> events = sessionEvents.get(sessionId);
        if (events == null || events.isEmpty()) {
            return;
        }
        
        Instant cutoff = Instant.now().minusMillis(maxAge);
        
        List<StoredEvent> toRemove = events.stream()
            .filter(event -> event.getTimestamp().isBefore(cutoff))
            .collect(Collectors.toList());
        
        for (StoredEvent event : toRemove) {
            events.remove(event);
            eventIdToSession.remove(event.getEventId());
        }
        
        if (!toRemove.isEmpty()) {
            logger.debug("Cleaned up {} old events for session {}", toRemove.size(), sessionId);
        }
    }
    
    @Override
    public void removeSessionEvents(String sessionId) {
        List<StoredEvent> events = sessionEvents.remove(sessionId);
        if (events != null) {
            for (StoredEvent event : events) {
                eventIdToSession.remove(event.getEventId());
            }
            logger.debug("Removed {} events for session {}", events.size(), sessionId);
        }
    }

    public void cleanupExpiredEvents() {
        for (String sessionId : sessionEvents.keySet()) {
            cleanupOldEvents(sessionId, defaultRetentionMs);
        }
    }
}
