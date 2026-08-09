/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.command;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import org.redisson.RedissonShutdownException;
import org.redisson.ScanResult;
import org.redisson.api.NodeType;
import org.redisson.cache.LRUCacheMap;
import org.redisson.client.*;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.CommandData;
import org.redisson.client.protocol.CommandsData;
import org.redisson.client.protocol.RedisCommand;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.client.protocol.decoder.ListMultiDecoder2;
import org.redisson.client.protocol.decoder.ObjectListReplayDecoder;
import org.redisson.config.DelayStrategy;
import org.redisson.config.ReadMode;
import org.redisson.connection.ClientConnectionsEntry;
import org.redisson.connection.ConnectionManager;
import org.redisson.connection.MasterSlaveEntry;
import org.redisson.connection.NodeSource;
import org.redisson.connection.NodeSource.Redirect;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.misc.LogHelper;
import org.redisson.misc.RedisURI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

/**
 * 单条 Redis 命令的执行引擎：获取连接、发送、超时/重试、MOVED/ASK 重定向、
 * 阻塞命令取消及结果解码。
 * <p>由 {@link CommandAsyncService#async} 创建并 {@link #execute()}；
 * 批量子类 {@link BaseRedisBatchExecutor} 覆写发送逻辑为入队。
 *
 * @author Nikita Koksharov
 *
 * @param <V> Redis 协议回复值类型
 * @param <R> 业务层期望的返回类型
 */
@SuppressWarnings({"NestedIfDepth", "ParameterNumber"})
public class RedisExecutor<V, R> {

    /** 日志记录器。 */
    static final Logger log = LoggerFactory.getLogger(RedisExecutor.class);

    /** true 时从读连接池取连接 */
    final boolean readOnlyMode;
    /** Redis 命令定义（含解码器） */
    final RedisCommand<V> command;
    /** 命令参数数组 */
    final Object[] params;
    /** 业务层等待的结果 Future */
    final CompletableFuture<R> mainPromise;
    /** 是否忽略 MOVED/ASK 自动重定向 */
    final boolean ignoreRedirect;
    final RedissonObjectBuilder objectBuilder;
    /** 连接与集群拓扑管理 */
    final ConnectionManager connectionManager;
    final RedissonObjectBuilder.ReferenceType referenceType;
    final boolean noRetry;
    /** 最大重试次数（含首次） */
    final int attempts;
    /** 重试间隔计算策略 */
    final DelayStrategy retryStrategy;
    /** 单次响应超时毫秒数 */
    final int responseTimeout;
    final boolean trackChanges;
    final ReadMode readMode;

    long retryInterval;
    CompletableFuture<RedisConnection> connectionFuture;
    boolean reuseConnection;
    NodeSource source;
    MasterSlaveEntry entry;
    Codec codec;
    /** 当前重试序号（从 0 开始） */
    volatile int attempt;
    volatile Optional<Timeout> timeout = Optional.empty();
    volatile BiConsumer<R, Throwable> mainPromiseListener;
    /** Netty 写操作 Future */
    volatile ChannelFuture writeFuture;
    /** 最近一次超时/写失败异常，供重试逻辑使用 */
    volatile RedisException exception;

    public RedisExecutor(boolean readOnlyMode, NodeSource source, Codec codec, RedisCommand<V> command,
                         Object[] params, CompletableFuture<R> mainPromise, boolean ignoreRedirect,
                         ConnectionManager connectionManager, RedissonObjectBuilder objectBuilder,
                         RedissonObjectBuilder.ReferenceType referenceType, boolean noRetry,
                         int retryAttempts, DelayStrategy retryStrategy, int responseTimeout,
                         boolean trackChanges, ReadMode readMode) {
        super();
        this.readOnlyMode = readOnlyMode;
        this.source = source;
        this.codec = codec;
        this.command = command;
        this.params = params;
        this.mainPromise = mainPromise;
        this.ignoreRedirect = ignoreRedirect;
        this.connectionManager = connectionManager;
        this.objectBuilder = objectBuilder;
        this.noRetry = noRetry;
        this.retryStrategy = retryStrategy;

        this.attempts = retryAttempts;
        this.responseTimeout = responseTimeout;
        this.referenceType = referenceType;
        this.trackChanges = trackChanges;
        this.readMode = readMode;
    }

    /** 命令执行主流程：校验 shutdown → 取连接 → 发送 → 注册超时与完成回调。 */
    public void execute() {
        if (mainPromise.isCancelled()) {
            free();
            return;
        }

        if (getClass() == RedisExecutor.class) {
            connectionManager.getServiceManager().addFuture(mainPromise);
        }

        if (connectionManager.getServiceManager().isShuttingDown()) {
            free();
            mainPromise.completeExceptionally(new RedissonShutdownException("Redisson is shutdown"));
            return;
        }

        try {
            codec = getCodec(codec);

            CompletableFuture<R> attemptPromise = new CompletableFuture<>();
            CompletableFuture<RedisConnection> connectionFuture = getConnection(attemptPromise);
            mainPromiseListener = (r, e) -> {
                if (!mainPromise.isCompletedExceptionally()) {
                    return;
                }

                if (connectionFuture.completeExceptionally(new CancellationException())) {
                    log.debug("Connection obtaining canceled for {}", command);
                    timeout.ifPresent(Timeout::cancel);
                    if (attemptPromise.completeExceptionally(new CancellationException())) {
                        free();
                    }
                    return;
                }

                if (command.isBlockingCommand()) {
                    if (writeFuture.cancel(false)) {
                        attemptPromise.completeExceptionally(new CancellationException());
                    } else {
                        RedisConnection c = connectionFuture.getNow(null);
                        c.forceFastReconnectAsync().whenComplete((res, ex) -> {
                            attemptPromise.completeExceptionally(new CancellationException());
                        });
                    }
                }
            };

            if (attempt == 0) {
                mainPromise.whenComplete((r, e) -> {
                    if (this.mainPromiseListener != null) {
                        this.mainPromiseListener.accept(r, e);
                    }
                });
            }

            retryInterval = retryStrategy.calcDelay(attempt).toMillis();

            scheduleRetryTimeout(connectionFuture, attemptPromise);

            scheduleConnectionTimeout(attemptPromise, connectionFuture);

            connectionFuture.whenComplete((connection, e) -> {
                if (connectionFuture.isCancelled()) {
                    return;
                }

                if (connectionManager.getServiceManager().isShuttingDown()) {
                    exception = new RedissonShutdownException("Redisson is shutdown");
                    tryComplete(attemptPromise, exception);
                    return;
                }

                if (connectionFuture.isDone() && connectionFuture.isCompletedExceptionally()) {
                    exception = convertException(connectionFuture);
                    tryComplete(attemptPromise, exception);
                    return;
                }

                try {
                    sendCommand(attemptPromise, connection);
                } catch (Exception ex) {
                    free();
                    handleError(connectionFuture, e);
                    return;
                }

                scheduleWriteTimeout(attemptPromise);

                writeFuture.addListener((ChannelFutureListener) future -> {
                    checkWriteFuture(writeFuture, attemptPromise, connection);
                });
            });

            attemptPromise.whenComplete((r, e) -> {
                releaseConnection(attemptPromise, connectionFuture);

                checkAttemptPromise(attemptPromise, connectionFuture);
            }).whenComplete((r, e) -> {
                if (e != null
                        && !attemptPromise.isCompletedExceptionally()) {
                    log.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            free();
            handleError(connectionFuture, e);
            throw e;
        }
    }

    /** 连接获取超时：无重试间隔时在 responseTimeout 内必须拿到连接。 */
    private void scheduleConnectionTimeout(CompletableFuture<R> attemptPromise, CompletableFuture<RedisConnection> connectionFuture) {
        if (retryInterval > 0 && attempts > 0) {
            return;
        }

        timeout.ifPresent(Timeout::cancel);

        TimerTask task = timeout -> {
            if (connectionFuture.completeExceptionally(new CancellationException())) {
                exception = new RedisTimeoutException("Unable to acquire connection! " + this.connectionFuture +
                        "Increase connection pool size or timeout. "
                        + "Node source: " + source
                        + ", " + LogHelper.toString(command, params)
                        + " after " + attempt + " of " + attempts + " retry attempts");

                attemptPromise.completeExceptionally(exception);
            }
        };

        timeout = Optional.of(connectionManager.getServiceManager().newTimeout(task, responseTimeout, TimeUnit.MILLISECONDS));
    }

    /** 写超时：命令在超时内必须写入 Netty Channel。 */
    private void scheduleWriteTimeout(CompletableFuture<R> attemptPromise) {
        if (retryInterval > 0 && attempts > 0) {
            return;
        }

        timeout.ifPresent(Timeout::cancel);

        TimerTask task = timeout -> {
            if (writeFuture.cancel(false)) {
                int pendingTasks = countPendingTasks();
                exception = new RedisTimeoutException("Command still hasn't been written into connection! " +
                        "Check CPU usage of the JVM. Check that there are no blocking invocations in async/reactive/rx listeners or subscribeOnElements method. Check connection with Redis node: " + connectionFuture.join().getRedisClient().getAddr() +
                        " for TCP packet drops. Try to increase nettyThreads setting."
                        + " Netty pending tasks: " + pendingTasks + ","
                        + " Node source: " + source + ", connection: " + connectionFuture.join()
                        + ", " + LogHelper.toString(command, params)
                        + " after " + attempt + " of " + attempts + " retry attempts");
                attemptPromise.completeExceptionally(exception);
            }
        };

        timeout = Optional.of(connectionManager.getServiceManager().newTimeout(task, responseTimeout, TimeUnit.MILLISECONDS));
    }

    /** 带重试间隔时周期性检查连接/写入进度，失败则递增 attempt 并重入 execute。 */
    private void scheduleRetryTimeout(CompletableFuture<RedisConnection> connectionFuture, CompletableFuture<R> attemptPromise) {
        if (retryInterval == 0 || attempts == 0) {
            return;
        }

        TimerTask retryTimerTask = t -> {
            if (attemptPromise.isDone()) {
                return;
            }

            if (command != null && command.isBlockingCommand() && !connectionFuture.isDone()) {
                if (attempt < attempts) {
                    attempt++;
                    scheduleRetryTimeout(connectionFuture, attemptPromise);
                    return;
                }

                exception = new RedisTimeoutException("Unable to acquire connection! "
                        + "Increase connection pool size. "
                        + "Node source: " + source
                        + ", " + LogHelper.toString(command, params)
                        + " after " + attempt + " of " + attempts + " retry attempts");
                connectionFuture.completeExceptionally(new CancellationException());
                attemptPromise.completeExceptionally(exception);
                return;
            }

            if (connectionFuture.completeExceptionally(new CancellationException())) {
                exception = new RedisTimeoutException("Unable to acquire connection! " + connectionFuture +
                            "Increase connection pool size. "
                            + "Node source: " + source
                            + ", " + LogHelper.toString(command, params)
                            + " after " + attempt + " of " + attempts + " retry attempts");
            } else {
                if (connectionFuture.isDone() && !connectionFuture.isCompletedExceptionally()) {
                    if (writeFuture == null || !writeFuture.isDone()) {
                        if (attempt == attempts) {
                            if (writeFuture != null && writeFuture.cancel(false)) {
                                if (exception == null) {
                                    int pendingTasks = countPendingTasks();
                                    exception = new RedisTimeoutException("Command still hasn't been written into connection! " +
                                            "Check CPU usage of the JVM. Check that there are no blocking invocations in async/reactive/rx listeners or subscribeOnElements method. Check connection with Redis node: " + getNow(connectionFuture).getRedisClient().getAddr() +
                                            " for TCP packet drops. Try to increase nettyThreads setting." +
                                            " Netty pending tasks: " + pendingTasks + ","
                                          + " Node source: " + source + ", connection: " + getNow(connectionFuture)
                                            + ", " + LogHelper.toString(command, params)
                                            + " after " + attempt + " of " + attempts + " retry attempts");
                                }
                                attemptPromise.completeExceptionally(exception);
                            }
                            return;
                        }
                        attempt++;

                        scheduleRetryTimeout(connectionFuture, attemptPromise);
                        return;
                    }

                    if (writeFuture.isSuccess()) {
                        return;
                    }
                }
            }

            if (mainPromise.isCompletedExceptionally()) {
                Throwable c = cause(mainPromise);
                if (c instanceof CancellationException || c instanceof RedissonShutdownException) {
                    if (attemptPromise.completeExceptionally(new CancellationException())) {
                        free();
                    }
                }
                return;
            }

            if (attempt == attempts) {
                // 具体异常已在连接或写 Future 回调中赋值
                if (exception != null) {
                    attemptPromise.completeExceptionally(exception);
                }
                return;
            }
            if (!attemptPromise.completeExceptionally(new CancellationException())) {
                return;
            }

            attempt++;
            if (log.isDebugEnabled()) {
                log.debug("attempt {} for {} to {}",
                        attempt, LogHelper.toString(command, params), source);
            }

            mainPromiseListener = null;

            execute();
        };

        timeout = Optional.of(connectionManager.getServiceManager().newTimeout(retryTimerTask, retryInterval, TimeUnit.MILLISECONDS));
    }
    
    protected void free() {
        free(params);
    }
    
    protected void free(Object[] params) {
        for (Object obj : params) {
            ReferenceCountUtil.safeRelease(obj);
        }
    }
    
    private void checkWriteFuture(ChannelFuture future, CompletableFuture<R> attemptPromise, RedisConnection connection) {
        if (future.isCancelled() || attemptPromise.isDone()) {
            return;
        }

        if (!future.isSuccess()) {
            int pendingTasks = countPendingTasks();
            exception = new WriteRedisConnectionException(
                    "Unable to write command into connection! Check CPU usage of the JVM. Try to increase nettyThreads setting. " +
                            "Netty pending tasks: " + pendingTasks + ", " +
                            "Node source: "
                    + source + ", connection: " + connection +
                    ", " + LogHelper.toString(command, params)
                    + " after " + attempt + " of " + attempts + " retry attempts",
                    future.cause());
            tryComplete(attemptPromise, exception);
            return;
        }

        scheduleResponseTimeout(attemptPromise, connection);
    }

    private int countPendingTasks() {
        int pendingTasks = 0;
        for (EventExecutor eventExecutor : connectionManager.getServiceManager().getGroup()) {
            if (eventExecutor instanceof SingleThreadEventExecutor) {
                SingleThreadEventExecutor singleThreadEventExecutor = (SingleThreadEventExecutor) eventExecutor;
                pendingTasks += singleThreadEventExecutor.pendingTasks();
            }
        }
        return pendingTasks;
    }

    private void tryComplete(CompletableFuture<R> attemptPromise, RedisException exception) {
        if (attempt == attempts) {
            attemptPromise.completeExceptionally(exception);
        } else if (retryInterval == 0) {
            attempt++;

            if (log.isDebugEnabled()) {
                log.debug("attempt {} for {} to {}",
                        attempt, LogHelper.toString(command, params), source);
            }

            mainPromiseListener = null;
            execute();
        }
    }

    /** 响应超时：阻塞命令会叠加 BLOCK 等待时间与额外 1 秒容错。 */
    private void scheduleResponseTimeout(CompletableFuture<R> attemptPromise, RedisConnection connection) {
        timeout.ifPresent(Timeout::cancel);

        long timeoutTime = responseTimeout;
        if (command != null && command.isBlockingCommand()) {
            long popTimeout = 0;
            if (RedisCommands.BLOCKING_COMMANDS.contains(command)) {
                for (int i = 0; i < params.length-1; i++) {
                    if ("BLOCK".equals(params[i])) {
                        popTimeout = Long.parseLong(params[i+1].toString());
                        break;
                    }
                }
            } else {
                if (RedisCommands.BZMPOP.getName().equals(command.getName())) {
                    popTimeout = Long.parseLong(params[0].toString()) * 1000;
                } else {
                    popTimeout = Long.parseLong(params[params.length - 1].toString()) * 1000;
                }
            }

            handleBlockingOperations(attemptPromise, connection, popTimeout);
            if (popTimeout == 0) {
                return;
            }
            timeoutTime += popTimeout;
            // Redis 阻塞命令超时精度问题，额外加 1 秒
            timeoutTime += 1000;
        }

        long timeoutAmount = timeoutTime;
        TimerTask timeoutResponseTask = timeout -> {
            if (isResendAllowed(attempt, attempts)) {
                if (!attemptPromise.completeExceptionally(new CancellationException())) {
                    return;
                }

                connectionManager.getServiceManager().newTimeout(t -> {
                    attempt++;
                    if (log.isDebugEnabled()) {
                        log.debug("response timeout. new attempt {} for {} node {}",
                                attempt, LogHelper.toString(command, params), source);
                    }

                    mainPromiseListener = null;
                    execute();
                }, retryInterval, TimeUnit.MILLISECONDS);
                return;
            }

            int pendingTasks = countPendingTasks();
            attemptPromise.completeExceptionally(
                    new RedisResponseTimeoutException("Redis server response timeout (" + timeoutAmount + " ms) occurred"
                            + " after " + attempt + " of " + attempts + " retry attempts,"
                            + " is non-idempotent command: " + (command != null && command.isNoRetry())
                            + " Check connection with Redis node: " + connection.getRedisClient().getAddr() + " for TCP packet drops or bandwidth limits. "
                            + " Try to increase nettyThreads and/or timeout settings."
                            + " Netty pending tasks: " + pendingTasks + ", "
                            + LogHelper.toString(command, params) + ", channel: " + connection.getChannel()));
        };

        timeout = Optional.of(connectionManager.getServiceManager().newTimeout(timeoutResponseTask, timeoutTime, TimeUnit.MILLISECONDS));
    }

    private boolean isResendAllowed(int attempt, int attempts) {
        return attempt < attempts
                && !noRetry
                    && (command == null || (!command.isBlockingCommand() && !command.isNoRetry()));
    }

    private Object emptyBlockingResult(RedisCommand<?> command) {
        String name = command.getName();
        if (RedisCommands.XREAD.getName().equals(name)
                || RedisCommands.XREADGROUP.getName().equals(name)) {
            return Collections.emptyMap();
        }

        // BLMPOP/BZMPOP 与列表变体命令名相同，需用实例身份区分
        // sibling variants (BLMPOP_VALUES, BZMPOP_SINGLE_LIST, BZMPOP_ENTRIES),
        // so we must match on instance identity, not name.
        if (command == RedisCommands.BLMPOP || command == RedisCommands.BZMPOP) {
            return Collections.emptyMap();
        }

        if (command.getReplayMultiDecoder() instanceof ObjectListReplayDecoder
                || command.getReplayMultiDecoder() instanceof ListMultiDecoder2) {
            return Collections.emptyList();
        }
        return null;
    }

    private void handleBlockingOperations(CompletableFuture<R> attemptPromise, RedisConnection connection, long popTimeout) {
        Timeout scheduledFuture;
        if (popTimeout != 0) {
            // 连接丢失时阻塞命令在 popTimeout 后返回空结果并强制重连
            scheduledFuture = connectionManager.getServiceManager().newTimeout(timeout -> {
                R res = (R) emptyBlockingResult(command);
                if (attemptPromise.complete(res)) {
                    connection.forceFastReconnectAsync();
                }
            }, popTimeout + 3000, TimeUnit.MILLISECONDS);
        } else {
            scheduledFuture = null;
        }

        mainPromise.whenComplete((res, e) -> {
            if (scheduledFuture != null) {
                scheduledFuture.cancel();
            }

            // 取消阻塞命令时强制快速重连以打断 BLPOP 等
            if ((mainPromise.isCancelled()
                    || e instanceof  InterruptedException)
                        && !attemptPromise.isDone()) {
                log.debug("Canceled blocking operation {} used {}", command, connection);
                connection.forceFastReconnectAsync().whenComplete((r, ex) -> {
                    attemptPromise.completeExceptionally(new CancellationException());
                });
                return;
            }

            if (connectionManager.getServiceManager().isShuttingDown(e)) {
                attemptPromise.completeExceptionally(e);
            }
        });
    }

    protected final Throwable cause(CompletableFuture<?> future) {
        try {
            future.getNow(null);
            return null;
        } catch (CompletionException ex2) {
            return ex2.getCause();
        } catch (CancellationException ex1) {
            return ex1;
        }
    }

    /** 单次 attempt 结束：处理重定向、LOADING 回退主节点、可重试异常及最终结果。 */
    protected void checkAttemptPromise(CompletableFuture<R> attemptFuture, CompletableFuture<RedisConnection> connectionFuture) {
        timeout.ifPresent(Timeout::cancel);

        if (attemptFuture.isCancelled()) {
            return;
        }

        try {
            mainPromiseListener = null;

            Throwable cause = cause(attemptFuture);
            if (cause instanceof RedisWrongPasswordException) {
                if (attempt < attempts) {
                    onException();

                    reuseConnection = true;
                    CompletionStage<Void> f = connectionFuture.join().forceFastReconnectAsync();
                    f.thenAccept(v -> {
                        attempt++;
                        execute();
                    });
                    return;
                }
            }

            if (cause instanceof RedisRedirectException && !ignoreRedirect) {
                RedisRedirectException ex = (RedisRedirectException) cause;
                if (source.getRedirect() == Redirect.MOVED
                        && source.getAddr().equals(ex.getUrl())) {
                    free();
                    mainPromise.completeExceptionally(new RedisException("MOVED redirection loop detected. Node " + source.getAddr() + " has further redirect to " + ex.getUrl()));
                    return;
                }

                Redirect reason = Redirect.REDIRECT;
                if (cause instanceof RedisMovedException) {
                    reason = Redirect.MOVED;
                }
                if (cause instanceof RedisAskException) {
                    reason = Redirect.ASK;
                }

                handleRedirect(ex, connectionFuture, reason);
                return;
            }

            if (cause instanceof RedisLoadingException) {
                RedisConnection connection = connectionFuture.getNow(null);
                if (connection != null) {
                    ClientConnectionsEntry ce = entry.getEntry(connection.getRedisClient());
                    if (ce != null
                            && ce.getNodeType() == NodeType.SLAVE
                            && entry.getConfig().isFallbackLoadingToMaster()) {
                        onException();
                        source = new NodeSource(entry.getClient());
                        execute();
                        return;
                    }
                }
            }

            if (cause instanceof RedisRetryException
                    || cause instanceof RedisReadonlyException
                        || (cause instanceof RedisReconnectedException
                                && (writeFuture.cancel(false) || isResendAllowed(attempt, attempts)))) {
                if (attempt < attempts) {
                    onException();
                    connectionManager.getServiceManager().newTimeout(timeout -> {
                        attempt++;
                        execute();
                    }, retryInterval, TimeUnit.MILLISECONDS);
                    return;
                }
            }

            free();

            handleResult(attemptFuture, connectionFuture);

        } catch (Exception e) {
            handleError(connectionFuture, e);
        }
    }

    /** 解析 MOVED/ASK 目标地址并更新 NodeSource 后重试。 */
    private void handleRedirect(RedisRedirectException ex, CompletableFuture<RedisConnection> connectionFuture, Redirect reason) {
        onException();

        CompletableFuture<RedisURI> ipAddrFuture = connectionManager.getServiceManager().resolveIP(ex.getUrl());
        ipAddrFuture.whenComplete((ip, e) -> {
            if (e != null) {
                free();
                handleError(connectionFuture, e);
                return;
            }
            source = new NodeSource(ex.getSlot(), ip, reason);
            execute();
        });
    }

    protected void handleResult(CompletableFuture<R> attemptPromise, CompletableFuture<RedisConnection> connectionFuture) throws ReflectiveOperationException {
        R res;
        try {
            res = attemptPromise.getNow(null);
        } catch (CompletionException e) {
            handleError(connectionFuture, e.getCause());
            return;
        } catch (CancellationException e) {
            handleError(connectionFuture, e);
            return;
        }

        if (res instanceof ScanResult) {
            ((ScanResult) res).setRedisClient(getNow(connectionFuture).getRedisClient());
        }

        handleSuccess(mainPromise, connectionFuture, res);
    }

    protected void onException() {
    }

    protected void handleError(CompletableFuture<RedisConnection> connectionFuture, Throwable cause) {
        mainPromise.completeExceptionally(cause);
        if (connectionFuture == null) {
            return;
        }

        RedisClient client = connectionFuture.join().getRedisClient();
        FailedNodeDetector detector = client.getConfig().getFailedNodeDetector();
        detector.onCommandFailed(cause);
        if (detector.isNodeFailed()) {
            log.error("Redis node {} has been marked as failed according to the detection logic defined in {}",
                            entry.getClient().getAddr(), detector);
            entry.shutdownAndReconnectAsync(client, cause);
        }
    }

    protected void handleSuccess(CompletableFuture<R> promise, CompletableFuture<RedisConnection> connectionFuture, R res) throws ReflectiveOperationException {
        if (objectBuilder != null) {
            promise.complete((R) objectBuilder.tryHandleReference(res, referenceType));
        } else {
            promise.complete(res);
        }
        connectionFuture.join().getRedisClient().getConfig().getFailedNodeDetector().onCommandSuccessful();
    }

    /** 发送单条或 ASKING+命令；小连接池下非阻塞命令发送后立即 release。 */
    protected void sendCommand(CompletableFuture<R> attemptPromise, RedisConnection connection) {
        if (source.getRedirect() == Redirect.ASK) {
            List<CommandData<?, ?>> list = new ArrayList<>(2);
            CompletableFuture<Void> promise = new CompletableFuture<>();
            list.add(new CommandData<>(promise, codec, RedisCommands.ASKING, new Object[]{}));
            list.add(new CommandData<>(attemptPromise, codec, command, params));
            CompletableFuture<Void> main = new CompletableFuture<>();
            writeFuture = connection.send(new CommandsData(main, list, false, false));
        } else {
            if (log.isDebugEnabled()) {
                String connectionType = " ";
                if (connection instanceof RedisPubSubConnection) {
                    connectionType = " pubsub ";
                }
                log.debug("acquired{}connection for {} from slot {} using node {}... {}",
                        connectionType, LogHelper.toString(command, params), source, connection.getRedisClient().getAddr(), connection);
            }
            writeFuture = connection.send(new CommandData<>(attemptPromise, codec, command, params));

            if (connectionManager.getServiceManager().getConfig().getMasterConnectionPoolSize() < 10
                    && !command.isBlockingCommand()) {
                release(connection);
            }
        }
    }

    protected void releaseConnection(CompletableFuture<R> attemptPromise, CompletableFuture<RedisConnection> connectionFuture) {
        if (connectionFuture.isDone() && connectionFuture.isCompletedExceptionally()) {
            return;
        }

        Throwable cause = cause(attemptPromise);
        if (cause instanceof RedisWrongPasswordException
                && attempt < attempts) {
            return;
        }

        RedisConnection connection = getNow(connectionFuture);
        if (connectionManager.getServiceManager().getConfig().getMasterConnectionPoolSize() < 10) {
            if (source.getRedirect() == Redirect.ASK
                    || getClass() != RedisExecutor.class
                        || (command != null && command.isBlockingCommand())) {
                release(connection);
            }
        } else {
            release(connection);
        }

        if (log.isDebugEnabled()) {
            String connectionType = " ";
            if (connection instanceof RedisPubSubConnection) {
                connectionType = " pubsub ";
            }

            log.debug("connection{}released for {} from slot {} using connection {}",
                    connectionType, LogHelper.toString(command, params), source, connection);
        }
    }

    private void release(RedisConnection connection) {
        if (readOnlyMode) {
            entry.releaseRead(connection, readMode);
        } else {
            entry.releaseWrite(connection);
        }
    }

    public RedisClient getRedisClient() {
        return getNow(connectionFuture).getRedisClient();
    }

    protected CompletableFuture<RedisConnection> getConnection(CompletableFuture<R> attemptPromise) {
        if (reuseConnection) {
            reuseConnection = false;
            return connectionFuture;
        }
        if (readOnlyMode) {
            connectionFuture = connectionReadOp(command, attemptPromise);
        } else {
            connectionFuture = connectionWriteOp(command, attemptPromise);
        }
        return connectionFuture;
    }

    private static final Map<ClassLoader, Map<Codec, Codec>> CODECS = new LRUCacheMap<>(100, 0, 0);

    /** 按线程上下文 ClassLoader 缓存并克隆 Codec（Kryo 等需类加载器）。 */
    protected final Codec getCodec(Codec codec) {
        if (codec == null) {
            return null;
        }

        if (!connectionManager.getServiceManager().getCfg().isUseThreadClassLoader()) {
            return codec;
        }

        for (Class<?> clazz : BaseCodec.SKIPPED_CODECS) {
            if (clazz.isAssignableFrom(codec.getClass())) {
                return codec;
            }
        }

        Codec codecToUse = codec;
        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        if (threadClassLoader != null) {
            Map<Codec, Codec> map = CODECS.computeIfAbsent(threadClassLoader, k ->
                                            new LRUCacheMap<>(200, 0, 0));
            codecToUse = map.get(codec);
            if (codecToUse == null) {
                try {
                    Constructor<? extends Codec> c = codec.getClass().getConstructor(ClassLoader.class, codec.getClass());
                    codecToUse = c.newInstance(threadClassLoader, codec);
                } catch (NoSuchMethodException | InvocationTargetException e) {
                    codecToUse = codec;
                    // 无 (ClassLoader, Codec) 构造器则使用原 Codec
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                map.put(codec, codecToUse);
            }
        }
        return codecToUse;
    }

    protected final <T> T getNow(CompletableFuture<T> future) {
        try {
            return future.getNow(null);
        } catch (Exception e) {
            return null;
        }
    }

    private <T> RedisException convertException(CompletableFuture<T> future) {
        Throwable cause = cause(future);
        if (cause instanceof RedisException) {
            return (RedisException) cause;
        }
        return new RedisException("Unexpected exception while processing command", cause);
    }

    final CompletableFuture<RedisConnection> connectionReadOp(RedisCommand<?> command, CompletableFuture<R> attemptPromise) {
        try {
            // TODO 后续改为完全异步解析 entry
            entry = getEntry(true);
        } catch (Exception e) {
            attemptPromise.completeExceptionally(e);
            CompletableFuture<RedisConnection> f = new CompletableFuture<>();
            f.completeExceptionally(e);
            return f;
        }
        if (entry == null) {
            CompletableFuture<RedisConnection> f = new CompletableFuture<>();
            f.completeExceptionally(connectionManager.getServiceManager().createNodeNotFoundException(source));
            return f;
        }

        if (source.getRedirect() != null) {
            return entry.connectionReadOp(command, source.getAddr());
        }
        if (source.getRedisClient() != null) {
            return entry.connectionReadOp(command, source.getRedisClient(), trackChanges, readMode);
        }

        return entry.connectionReadOp(command, trackChanges, readMode);
    }

    final CompletableFuture<RedisConnection> connectionWriteOp(RedisCommand<?> command, CompletableFuture<R> attemptPromise) {
        try {
            // TODO make the method async
            entry = getEntry(false);
        } catch (Exception e) {
            attemptPromise.completeExceptionally(e);
            CompletableFuture<RedisConnection> f = new CompletableFuture<>();
            f.completeExceptionally(e);
            return f;
        }
        if (entry == null) {
            CompletableFuture<RedisConnection> f = new CompletableFuture<>();
            f.completeExceptionally(connectionManager.getServiceManager().createNodeNotFoundException(source));
            return f;
        }
        // ASK 重定向到从节点且该从属于当前 entry 时使用专用写连接
        if (source.getRedirect() != null
                && !source.getAddr().equals(entry.getClient().getAddr())
                && entry.hasSlave(source.getAddr())) {
            return entry.redirectedConnectionWriteOp(command, source.getAddr());
        }
        return entry.connectionWriteOp(command);
    }

    private MasterSlaveEntry getEntry(boolean read) {
        if (source.getRedirect() != null) {
            return connectionManager.getEntry(source.getAddr());
        }

        MasterSlaveEntry entry = source.getEntry();
        if (source.getRedisClient() != null) {
            entry = connectionManager.getEntry(source.getRedisClient());
        }
        if (entry == null && source.getSlot() != null) {
            if (read) {
                entry = connectionManager.getReadEntry(source.getSlot());
            } else {
                entry = connectionManager.getWriteEntry(source.getSlot());
            }
        }
        return entry;
    }

}
