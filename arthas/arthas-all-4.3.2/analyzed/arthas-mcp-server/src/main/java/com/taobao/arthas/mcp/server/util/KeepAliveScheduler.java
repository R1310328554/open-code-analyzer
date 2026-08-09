/**
 * Copyright 2025 - 2025 the original author or authors.
 */

package com.taobao.arthas.mcp.server.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.taobao.arthas.mcp.server.protocol.spec.McpSchema;
import com.taobao.arthas.mcp.server.protocol.spec.McpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * 定时向 MCP 客户端发送 keep-alive（ping）消息，防止空闲连接被超时断开。
 * <p>
 * 通过 {@link Supplier} 获取当前活跃会话集合，按固定间隔向每个会话发送 ping 请求。
 *
 * @see McpSession
 * @see McpSchema#METHOD_PING
 */
public class KeepAliveScheduler {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private static final TypeReference<Object> OBJECT_TYPE_REF = new TypeReference<Object>() {
    };

    /** 首次 keep-alive 调用前的初始延迟。 */
    private final Duration initialDelay;

    /** 后续 keep-alive 调用之间的间隔。 */
    private final Duration interval;

    /** 执行 keep-alive 任务的调度器。 */
    private final ScheduledExecutorService scheduler;

    /** 本调度器是否拥有 executor 并在 shutdown 时负责关闭它。 */
    private final boolean ownsExecutor;

    /** 调度器当前是否处于运行状态。 */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /** 当前已调度的定时任务句柄。 */
    private volatile ScheduledFuture<?> currentTask;

    /** 提供当前 {@link McpSession} 实例集合的供应器。 */
    private final Supplier<? extends Collection<? extends McpSession>> mcpSessions;

    /**
     * 创建 KeepAliveScheduler。
     * @param scheduler 执行 keep-alive 的调度器
     * @param ownsExecutor 是否由本调度器负责关闭 executor
     * @param initialDelay 首次 keep-alive 前的延迟
     * @param interval 后续 keep-alive 间隔
     * @param mcpSessions 会话集合供应器
     */
    private KeepAliveScheduler(ScheduledExecutorService scheduler, boolean ownsExecutor, Duration initialDelay,
                            Duration interval, Supplier<? extends Collection<? extends McpSession>> mcpSessions) {
        this.scheduler = scheduler;
        this.ownsExecutor = ownsExecutor;
        this.initialDelay = initialDelay;
        this.interval = interval;
        this.mcpSessions = mcpSessions;
    }

    /** 创建 Builder，需传入会话集合供应器。 */
    public static Builder builder(Supplier<? extends Collection<? extends McpSession>> mcpSessions) {
        return new Builder(mcpSessions);
    }

    /**
     * 启动定时 keep-alive 任务。
     * @return 当前实例，支持链式调用
     * @throws IllegalStateException 调度器已在运行时
     */
    public KeepAliveScheduler start() {
        if (this.isRunning.compareAndSet(false, true)) {
            logger.debug("Starting KeepAlive scheduler with initial delay: {}ms, interval: {}ms", 
                        initialDelay.toMillis(), interval.toMillis());

            this.currentTask = this.scheduler.scheduleAtFixedRate(
                this::sendKeepAlivePings,
                this.initialDelay.toMillis(),
                this.interval.toMillis(),
                TimeUnit.MILLISECONDS
            );

            return this;
        } else {
            throw new IllegalStateException("KeepAlive scheduler is already running. Stop it first.");
        }
    }

    /** 向所有活跃会话发送 keep-alive ping 请求。 */
    private void sendKeepAlivePings() {
        try {
            Collection<? extends McpSession> sessions = this.mcpSessions.get();
            if (sessions == null || sessions.isEmpty()) {
                logger.trace("No active sessions to ping");
                return;
            }

            logger.trace("Sending keep-alive pings to {} sessions", sessions.size());
            
            for (McpSession session : sessions) {
                try {
                    session.sendRequest(McpSchema.METHOD_PING, null, OBJECT_TYPE_REF)
                        .whenComplete((result, error) -> {
                            if (error != null) {
                                logger.warn("Failed to send keep-alive ping to session {}: {}", 
                                           session, error.getMessage());
                            } else {
                                logger.trace("Keep-alive ping sent successfully to session {}", session);
                            }
                        });
                } catch (Exception e) {
                    logger.warn("Exception while sending keep-alive ping to session {}: {}", 
                               session, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error during keep-alive ping cycle", e);
        }
    }

    /** 停止定时任务，但不关闭 executor（除非调用 {@link #shutdown()}）。 */
    public void stop() {
        if (this.currentTask != null && !this.currentTask.isCancelled()) {
            this.currentTask.cancel(false);
            logger.debug("KeepAlive scheduler stopped");
        }
        this.isRunning.set(false);
    }

    /** 返回调度器是否正在运行。 */
    public boolean isRunning() {
        return this.isRunning.get();
    }

    /**
     * 停止调度并关闭 executor（仅当 {@link #ownsExecutor} 为 true 时）。
     */
    public void shutdown() {
        stop();
        if (this.ownsExecutor && !this.scheduler.isShutdown()) {
            this.scheduler.shutdown();
            try {
                if (!this.scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    this.scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            logger.debug("KeepAlive scheduler executor shut down");
        }
    }

    /** Builder 模式构建 {@link KeepAliveScheduler}。 */
    public static class Builder {

        private ScheduledExecutorService scheduler;
        private boolean ownsExecutor = false;
        private Duration initialDelay = Duration.ofSeconds(0);
        private Duration interval = Duration.ofSeconds(30);
        private Supplier<? extends Collection<? extends McpSession>> mcpSessions;

        Builder(Supplier<? extends Collection<? extends McpSession>> mcpSessions) {
            Assert.notNull(mcpSessions, "McpSessions supplier must not be null");
            this.mcpSessions = mcpSessions;
        }

        /** 指定外部调度器（不由 Builder 创建，故 ownsExecutor 为 false）。 */
        public Builder scheduler(ScheduledExecutorService scheduler) {
            Assert.notNull(scheduler, "Scheduler must not be null");
            this.scheduler = scheduler;
            this.ownsExecutor = false;
            return this;
        }

        /** 设置首次 keep-alive 前的初始延迟。 */
        public Builder initialDelay(Duration initialDelay) {
            Assert.notNull(initialDelay, "Initial delay must not be null");
            this.initialDelay = initialDelay;
            return this;
        }

        /** 设置 keep-alive 调用间隔，默认 30 秒。 */
        public Builder interval(Duration interval) {
            Assert.notNull(interval, "Interval must not be null");
            this.interval = interval;
            return this;
        }

        /**
         * 构建调度器；未指定 scheduler 时自动创建单线程守护调度器。
         */
        public KeepAliveScheduler build() {
            if (this.scheduler == null) {
                this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "mcp-keep-alive-scheduler");
                    t.setDaemon(true);
                    return t;
                });
                this.ownsExecutor = true;
            }
            
            return new KeepAliveScheduler(scheduler, ownsExecutor, initialDelay, interval, mcpSessions);
        }
    }
}
