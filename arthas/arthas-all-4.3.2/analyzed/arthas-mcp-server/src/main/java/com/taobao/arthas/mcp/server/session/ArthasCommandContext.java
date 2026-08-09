package com.taobao.arthas.mcp.server.session;

import com.taobao.arthas.mcp.server.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * MCP 服务端命令执行上下文，封装 {@link CommandExecutor} 与可选的会话绑定。
 * <p>
 * 负责同步/异步执行 Arthas 命令、收集输出结果，并在会话模式下管理认证与用户标识。
 */
public class ArthasCommandContext {

    private static final Logger logger = LoggerFactory.getLogger(ArthasCommandContext.class);

    /** 同步命令执行默认超时（毫秒）。 */
    private static final long DEFAULT_SYNC_TIMEOUT = 30000L;

    /** 底层命令执行器，实际调用 Arthas 内核。 */
    private final CommandExecutor commandExecutor;
    /** 与 MCP 会话绑定的 Arthas session；临时模式下为 null。 */
    private final ArthasCommandSessionManager.CommandSessionBinding binding;
    /** 标记当前命令执行是否已结束。 */
    private volatile boolean executionComplete = false;
    /** 命令输出片段列表，线程安全追加。 */
    private final List<Object> results = new CopyOnWriteArrayList<>();
    /** 保护结果读写与完成状态的互斥锁。 */
    private final Lock resultLock = new ReentrantLock();

    /** 临时模式构造：无会话绑定，仅支持同步执行。 */
    public ArthasCommandContext(CommandExecutor commandExecutor) {
        this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor cannot be null");
        this.binding = null;
    }

    /** 会话模式构造：绑定 MCP/Arthas 会话，支持异步拉取与中断。 */
    public ArthasCommandContext(CommandExecutor commandExecutor, ArthasCommandSessionManager.CommandSessionBinding binding) {
        this.commandExecutor = Objects.requireNonNull(commandExecutor, "commandExecutor cannot be null");
        this.binding = binding;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }

    public String getSessionId() {
        return binding != null ? binding.getArthasSessionId() : null;
    }

    /**
     * {@link #getSessionId()} 的别名，供需要显式 Arthas session ID 的调用方使用。
     */
    public String getArthasSessionId() {
        requireSessionSupport();
        return binding.getArthasSessionId();
    }

    /** 校验当前上下文已绑定会话，否则抛出非法状态异常。 */
    private void requireSessionSupport() {
        if (binding == null) {
            throw new IllegalStateException("Session-based operations are not supported in temporary mode. " +
                    "Use ArthasCommandContext(CommandExecutor, CommandSessionBinding) constructor to enable session support.");
        }
    }
    
    public String getConsumerId() {
        return binding != null ? binding.getConsumerId() : null;
    }

    public ArthasCommandSessionManager.CommandSessionBinding getBinding() {
        return binding;
    }

    public boolean isExecutionComplete() {
        return executionComplete;
    }

    public void setExecutionComplete(boolean executionComplete) {
        this.executionComplete = executionComplete;
    }

    public void addResult(Object result) {
        results.add(result);
    }

    public List<Object> getResults() {
        return results;
    }

    public void clearResults() {
        results.clear();
    }

    public Lock getResultLock() {
        return resultLock;
    }

    /**
     * 以默认超时同步执行命令行。
     */
    public Map<String, Object> executeSync(String commandLine) {
        return executeSync(commandLine, DEFAULT_SYNC_TIMEOUT);
    }

    /**
     * 以指定超时同步执行命令行。
     */
    public Map<String, Object> executeSync(String commandLine, long timeout) {
        return commandExecutor.executeSync(commandLine, timeout);
    }

    /**
     * 携带认证主体同步执行命令（无 userId 统计）。
     */
    public Map<String, Object> executeSync(String commandStr, Object authSubject) {
        return commandExecutor.executeSync(commandStr, DEFAULT_SYNC_TIMEOUT, null, authSubject, null);
    }

    /**
     * Execute command synchronously with auth subject and userId
     *
     * @param commandStr 命令行
     * @param authSubject 认证主体
     * @param userId 用户 ID，用于统计上报
     * @return 执行结果
     */
    public Map<String, Object> executeSync(String commandStr, Object authSubject, String userId) {
        return commandExecutor.executeSync(commandStr, DEFAULT_SYNC_TIMEOUT, null, authSubject, userId);
    }

    /**
     * 在绑定的 Arthas 会话上异步提交命令，立即返回。
     */
    public Map<String, Object> executeAsync(String commandLine) {
        requireSessionSupport();
        return commandExecutor.executeAsync(commandLine, binding.getArthasSessionId());
    }

    /**
     * 按 consumerId 从会话拉取异步命令的输出块。
     */
    public Map<String, Object> pullResults() {
        requireSessionSupport();
        return commandExecutor.pullResults(binding.getArthasSessionId(), binding.getConsumerId());
    }

    /**
     * 中断当前会话上正在执行的 Arthas 作业。
     */
    public Map<String, Object> interruptJob() {
        if (binding != null) {
            return commandExecutor.interruptJob(binding.getArthasSessionId());
        }
        return null;
    }

    /** 将会话关联的用户 ID 写入 Arthas session，用于统计上报。 */
    public void setSessionUserId(String userId) {
        if (binding != null && userId != null) {
            commandExecutor.setSessionUserId(binding.getArthasSessionId(), userId);
            logger.debug("Set userId for session {}: {}", binding.getArthasSessionId(), userId);
        }
    }

    /**
     * 将已认证主体写入当前绑定的 Arthas session。
     *
     * @param authSubject 认证主体
     */
    public void setSessionAuth(Object authSubject) {
        if (binding != null && authSubject != null) {
            commandExecutor.setSessionAuth(binding.getArthasSessionId(), authSubject);
            logger.debug("Set auth subject for session {}: {}",
                    binding.getArthasSessionId(), authSubject.getClass().getSimpleName());
        }
    }
}
