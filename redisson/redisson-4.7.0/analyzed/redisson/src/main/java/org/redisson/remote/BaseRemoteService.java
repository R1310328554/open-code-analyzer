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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.redisson.RedissonBlockingQueue;
import org.redisson.RedissonMap;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RemoteInvocationOptions;
import org.redisson.api.annotation.RRemoteAsync;
import org.redisson.api.annotation.RRemoteReactive;
import org.redisson.api.annotation.RRemoteRx;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.executor.RemotePromise;
import org.redisson.misc.Hash;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分布式远程服务（RRemoteService）的核心基类。
 * <p>
 * 根据接口注解 {@link RRemoteAsync}、{@link RRemoteReactive}、{@link RRemoteRx}
 * 或默认同步模式，创建对应 {@link BaseRemoteProxy} 并返回动态代理。
 * <p>
 * 管理请求/响应/取消相关的 Redis 键名、方法签名哈希与队列读写。
 *
 * @author Nikita Koksharov
 *
 */
public abstract class BaseRemoteService {

    /** 接口 → 请求队列名缓存。 */
    private final Map<Class<?>, String> requestQueueNameCache = new ConcurrentHashMap<>();
    /** 方法 → Murmur128 参数类型签名，用于远程方法匹配。 */
    private final ConcurrentMap<Method, long[]> methodSignaturesCache = new ConcurrentHashMap<>();

    protected final Codec codec;
    protected final String name;
    protected final CommandAsyncExecutor commandExecutor;
    protected final String executorId;

    /** 客户端取消请求 Map 键名。 */
    protected final String cancelRequestMapName;
    /** Worker 取消响应 Map 键名。 */
    protected final String cancelResponseMapName;
    /** 本 executorId 的响应队列名。 */
    protected final String responseQueueName;

    /** 初始化编解码、映射后的服务名及取消/响应 Redis 键。 */
    public BaseRemoteService(Codec codec, String name, CommandAsyncExecutor commandExecutor, String executorId) {
        this.codec = commandExecutor.getServiceManager().getCodec(codec);
        this.name = commandExecutor.getServiceManager().getNameMapper().map(name);
        this.commandExecutor = commandExecutor;
        this.executorId = executorId;
        this.cancelRequestMapName = "{" + this.name + ":remote" + "}:cancel-request";
        this.cancelResponseMapName = "{" + this.name + ":remote" + "}:cancel-response";
        this.responseQueueName = getResponseQueueName(executorId);
    }

    /** @param executorId 客户端实例 ID @return 响应队列 Redis 键 */
    public String getResponseQueueName(String executorId) {
        return "{remote_response}:" + executorId;
    }
    
    /** ACK 确认键：{name:remote}:requestId:ack */
    protected String getAckName(String requestId) {
        return "{" + name + ":remote" + "}:" + requestId + ":ack";
    }
    
    public String getRequestQueueName(Class<?> remoteInterface) {
        return requestQueueNameCache.computeIfAbsent(remoteInterface, k -> "{" + name + ":" + k.getName() + "}");
    }

    /** 使用服务 Codec 编码请求/响应体。 */
    protected ByteBuf encode(Object obj) {
        try {
            return codec.getValueEncoder().encode(obj);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** 默认远程调用选项（ACK + 结果超时均为默认）。 */
    public <T> T get(Class<T> remoteInterface) {
        return get(remoteInterface, RemoteInvocationOptions.defaults());
    }

    public <T> T get(Class<T> remoteInterface, long executionTimeout, TimeUnit executionTimeUnit) {
        return get(remoteInterface,
                RemoteInvocationOptions.defaults().expectResultWithin(executionTimeout, executionTimeUnit));
    }

    public <T> T get(Class<T> remoteInterface, long executionTimeout, TimeUnit executionTimeUnit, long ackTimeout,
            TimeUnit ackTimeUnit) {
        return get(remoteInterface, RemoteInvocationOptions.defaults().expectAckWithin(ackTimeout, ackTimeUnit)
                .expectResultWithin(executionTimeout, executionTimeUnit));
    }

    /** 按接口注解选择 Async/Reactive/Rx/Sync 代理并 create。 */
    public <T> T get(Class<T> remoteInterface, RemoteInvocationOptions options) {
        for (Annotation annotation : remoteInterface.getAnnotations()) {
            // @RRemoteAsync → AsyncRemoteProxy，返回 RFuture
            if (annotation.annotationType() == RRemoteAsync.class) {
                Class<T> syncInterface = (Class<T>) ((RRemoteAsync) annotation).value();
                AsyncRemoteProxy proxy = new AsyncRemoteProxy(commandExecutor, name, responseQueueName,
                        codec, executorId, cancelRequestMapName, this);
                return proxy.create(remoteInterface, options, syncInterface);
            }

            // @RRemoteReactive → ReactiveRemoteProxy，返回 Mono
            if (annotation.annotationType() == RRemoteReactive.class) {
                Class<T> syncInterface = (Class<T>) ((RRemoteReactive) annotation).value();
                ReactiveRemoteProxy proxy = new ReactiveRemoteProxy(commandExecutor, name, responseQueueName,
                        codec, executorId, cancelRequestMapName, this);
                return proxy.create(remoteInterface, options, syncInterface);
            }

            if (annotation.annotationType() == RRemoteRx.class) {
                Class<T> syncInterface = (Class<T>) ((RRemoteRx) annotation).value();
                RxRemoteProxy proxy = new RxRemoteProxy(commandExecutor, name, responseQueueName,
                        codec, executorId, cancelRequestMapName, this);
                return proxy.create(remoteInterface, options, syncInterface);
            }
        }

        SyncRemoteProxy proxy = new SyncRemoteProxy(commandExecutor, name, responseQueueName, codec, executorId, this);
        return proxy.create(remoteInterface, options);
    }

    /** 子类可覆盖：根据请求计算实际执行超时毫秒数。 */
    protected long getTimeout(Long executionTimeoutInMillis, RemoteServiceRequest request) {
        return executionTimeoutInMillis;
    }

    /** 创建不触发 name 重映射的 RedissonMap（用于 cancel 等内部键）。 */
    protected <K, V> RMap<K, V> getMap(String name) {
        return new RedissonMap(new CompositeCodec(StringCodec.INSTANCE, codec, codec), commandExecutor, name) {
            @Override
            protected void setName(String name) {
                this.name = name;
            }
        };
    }
    
    /** 每 3s 轮询 cancel Map，直到读到取消请求或 Future 已完成。 */
    protected <T> void scheduleCheck(String mapName, String requestId, CompletableFuture<T> cancelRequest) {
        commandExecutor.getServiceManager().newTimeout(timeout -> {
            if (cancelRequest.isDone()) {
                return;
            }

            RMap<String, T> canceledRequests = getMap(mapName);
            RFuture<T> future = canceledRequests.removeAsync(requestId);
            future.whenComplete((request, ex) -> {
                if (cancelRequest.isDone()) {
                    return;
                }
                if (ex != null) {
                    scheduleCheck(mapName, requestId, cancelRequest);
                    return;
                }

                if (request == null) {
                    scheduleCheck(mapName, requestId, cancelRequest);
                } else {
                    cancelRequest.complete(request);
                }
            });
        }, 3000, TimeUnit.MILLISECONDS);
    }

    /** 生成全局唯一 requestId（默认与参数无关）。 */
    protected String generateRequestId(Object[] args) {
        return commandExecutor.getServiceManager().generateId();
    }

    protected abstract CompletableFuture<Boolean> addAsync(String requestQueueName, RemoteServiceRequest request,
                                                           RemotePromise<Object> result);

    protected abstract CompletableFuture<Boolean> removeAsync(String requestQueueName, String taskId);

    /** 对方法参数类型名拼接后 Murmur128，供 Worker 匹配重载。 */
    protected long[] getMethodSignature(Method method) {
        return methodSignaturesCache.computeIfAbsent(method, m -> {
            String str = Arrays.stream(m.getParameterTypes())
                                .map(c -> c.getName())
                                .collect(Collectors.joining());
            ByteBuf buf = Unpooled.copiedBuffer(str, CharsetUtil.UTF_8);
            long[] result = Hash.hash128(buf);
            buf.release();
            return result;
        });
    }

    protected <V> RBlockingQueue<V> getBlockingQueue(String name, Codec codec) {
        return new RedissonBlockingQueue<>(codec, commandExecutor, name);
    }

}
