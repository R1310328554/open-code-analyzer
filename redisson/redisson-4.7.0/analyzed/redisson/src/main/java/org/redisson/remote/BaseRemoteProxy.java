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
package org.redisson.remote;

import io.netty.util.Timeout;
import org.redisson.RedissonBlockingQueue;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RFuture;
import org.redisson.api.RemoteInvocationOptions;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.LongCodec;
import org.redisson.client.protocol.RedisCommands;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.remote.ResponseEntry.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * 远程服务客户端代理基类：管理响应队列轮询与 ACK 重试。
 * <p>
 * 全局 {@link ResponseEntry} 映射按响应队列名索引待完成的
 * {@link CompletableFuture}；后台 {@link #pollResponse} 从
 * {@link RBlockingQueue} 异步取响应并匹配 requestId。
 * <p>
 * {@link AsyncRemoteProxy}、{@link SyncRemoteProxy} 等子类继承此类。
 *
 * @author Nikita Koksharov
 *
 */
public abstract class BaseRemoteProxy {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    /** 命令执行器。 */
    final CommandAsyncExecutor commandExecutor;
    /** 远程服务命名空间（Redis 键前缀）。 */
    private final String name;
    /** 本客户端专属的响应队列名。 */
    final String responseQueueName;
    /** 全局响应等待表（由 ServiceManager 共享）。 */
    final Codec codec;
    final String executorId;
    final BaseRemoteService remoteService;

    BaseRemoteProxy(CommandAsyncExecutor commandExecutor, String name, String responseQueueName,
                    Codec codec, String executorId, BaseRemoteService remoteService) {
        super();
        this.commandExecutor = commandExecutor;
        this.name = name;
        this.responseQueueName = responseQueueName;
        this.responses = commandExecutor.getServiceManager().getResponses();
        this.codec = codec;
        this.executorId = executorId;
        this.remoteService = remoteService;
    }

    /** 远程接口 → 请求队列 Redis 键名缓存。 */
    
    /** 请求队列键：{name:接口全限定名}，保证同 slot。 */
    public String getRequestQueueName(Class<?> remoteInterface) {
        return requestQueueNameCache.computeIfAbsent(remoteInterface, k -> "{" + name + ":" + k.getName() + "}");
    }
    
    /** ACK 首次未到时用 Lua 竞争 ack 键，成功则延长轮询等待 Worker ACK。 */
    protected CompletionStage<RemoteServiceAck> tryPollAckAgainAsync(RemoteInvocationOptions optionsCopy,
                                                                     String ackName, String requestId) {
        RFuture<Boolean> ackClientsFuture = commandExecutor.evalWriteNoRetryAsync(ackName, LongCodec.INSTANCE, RedisCommands.EVAL_BOOLEAN,
                    "if redis.call('setnx', KEYS[1], 1) == 1 then " 
                        + "redis.call('pexpire', KEYS[1], ARGV[1]);"
                        + "return 0;" 
                    + "end;" 
                    + "redis.call('del', KEYS[1]);" 
                    + "return 1;",
                Arrays.asList(ackName), optionsCopy.getAckTimeoutInMillis());
        return ackClientsFuture.thenCompose(res -> {
            if (res) {
                return pollResponse(commandExecutor.getServiceManager().getConfig().getTimeout(), requestId, true);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /** 注册 requestId 对应的响应 Future，并启动/续跑响应队列消费。 */
    protected final <T extends RRemoteServiceResponse> CompletableFuture<T> pollResponse(long timeout,
                                                                                         String requestId, boolean insertFirst) {
        CompletableFuture<T> responseFuture = new CompletableFuture<T>();

        ResponseEntry e = responses.compute(responseQueueName, (key, entry) -> {
            if (entry == null) {
                entry = new ResponseEntry();
            }

            addCancelHandling(requestId, responseFuture);

            Result res = new Result(responseFuture);

            Timeout responseTimeoutFuture = createResponseTimeout(timeout, requestId, responseFuture, res);
            res.setResponseTimeoutFuture(responseTimeoutFuture);

            Map<String, List<Result>> entryResponses = entry.getResponses();
            List<Result> list = entryResponses.computeIfAbsent(requestId, k -> new ArrayList<>(3));

            if (insertFirst) {
                list.add(0, res);
            } else {
                list.add(res);
            }
            return entry;
        });

        if (e.getStarted().compareAndSet(false, true)) {
            pollResponse(e);
        }

        return responseFuture;
    }

    /** 响应等待超时：完成 Future 并清理 ResponseEntry 中的占位。 */
    private <T extends RRemoteServiceResponse> Timeout createResponseTimeout(long timeout, String requestId,
                                                                             CompletableFuture<T> responseFuture, Result res) {
        return commandExecutor.getServiceManager().newTimeout(t -> {
                    responses.computeIfPresent(responseQueueName, (k, entry) -> {
                        RemoteServiceTimeoutException ex = new RemoteServiceTimeoutException("No response after " + timeout + "ms");
                        if (!responseFuture.completeExceptionally(ex)) {
                            return entry;
                        }

                        List<Result> list = entry.getResponses().get(requestId);
                        list.remove(res);
                        if (list.isEmpty()) {
                            entry.getResponses().remove(requestId);
                        }
                        if (entry.getResponses().isEmpty()) {
                            return null;
                        }
                        return entry;
                    });
                }, timeout, TimeUnit.MILLISECONDS);
    }

    /** Future 被取消时移除 ResponseEntry 中对应 Result 并取消 Netty 超时。 */
    private <T extends RRemoteServiceResponse> void addCancelHandling(String requestId, CompletableFuture<T> responseFuture) {
        responseFuture.whenComplete((res, ex) -> {
            if (!responseFuture.isCancelled()) {
                return;
            }

            responses.computeIfPresent(responseQueueName, (key, e) -> {
                List<Result> list = e.getResponses().get(requestId);
                if (list == null) {
                    return e;
                }

                for (Iterator<Result> iterator = list.iterator(); iterator.hasNext();) {
                    Result result = iterator.next();
                    if (result.getPromise() == responseFuture) {
                        result.cancelResponseTimeout();
                        iterator.remove();
                    }
                }
                if (list.isEmpty()) {
                    e.getResponses().remove(requestId);
                }

                if (e.getResponses().isEmpty()) {
                    return null;
                }
                return e;
            });
        });
    }

    /** 从响应阻塞队列 pollAsync，60s 无消息则重新 poll。 */
    private void pollResponse(ResponseEntry owner) {
        if (responses.get(responseQueueName) != owner) {
            return;
        }
        RBlockingQueue<RRemoteServiceResponse> queue = new RedissonBlockingQueue<>(codec, commandExecutor, responseQueueName);
        RFuture<RRemoteServiceResponse> future = queue.pollAsync(60, TimeUnit.SECONDS);
        future.whenComplete(createResponseListener(owner));
    }

    private BiConsumer<RRemoteServiceResponse, Throwable> createResponseListener(ResponseEntry owner) {
        return (response, e) -> {
            if (e != null) {
                if (commandExecutor.getServiceManager().isShuttingDown(e)) {
                    return;
                }

                log.error("Can't get response from {}. Try to increase 'retryDelay' and/or 'retryAttempts' settings", responseQueueName, e);
                return;
            }

            if (response == null) {
                pollResponse(owner);
                return;
            }

            AtomicReference<CompletableFuture<RRemoteServiceResponse>> future = new AtomicReference<>();
            responses.computeIfPresent(responseQueueName, (k, entry) -> {
                String key = response.getId();
                List<Result> list = entry.getResponses().get(key);
                if (list == null) {
                    pollResponse(owner);
                    return entry;
                }

                boolean isResultResponse = response instanceof RemoteServiceResponse;
                // 结果响应到达时丢弃队列中积压的旧 ACK（#5146）
                if (isResultResponse) {
                    // drain stale acks, see #5146 for details
                    while (list.size() > 1) {
                        Result stale = list.remove(0);
                        CompletableFuture<RRemoteServiceResponse> f = stale.getPromise();
                        stale.cancelResponseTimeout();
                        f.complete(new RemoteServiceAck(response.getId()));
                    }
                }

                Result res = list.remove(0);
                if (list.isEmpty()) {
                    entry.getResponses().remove(key);
                }

                CompletableFuture<RRemoteServiceResponse> f = res.getPromise();
                res.cancelResponseTimeout();
                future.set(f);

                if (entry.getResponses().isEmpty()) {
                    return null;
                }

                pollResponse(owner);
                return entry;
            });

            if (future.get() != null) {
                future.get().complete(response);
            }
        };
    }
    
}
