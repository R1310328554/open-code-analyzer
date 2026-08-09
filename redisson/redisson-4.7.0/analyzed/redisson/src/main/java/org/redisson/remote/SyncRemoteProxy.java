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

import org.redisson.RedissonBucket;
import org.redisson.api.RemoteInvocationOptions;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.executor.RemotePromise;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * 同步阻塞式远程服务 JDK 动态代理：
 * 通过 {@link Proxy} 拦截接口方法调用，将请求写入 Redis 队列并
 * 阻塞等待 ACK（可选）与 {@link RemoteServiceResponse}。
 * <p>
 * 适用于传统同步业务代码；超时与无结果选项由
 * {@link RemoteInvocationOptions} 控制。
 *
 * @author Nikita Koksharov
 *
 */
public class SyncRemoteProxy extends BaseRemoteProxy {

    /** @param commandExecutor 异步命令执行器 @param name 服务名 @param responseQueueName 响应队列 */
    public SyncRemoteProxy(CommandAsyncExecutor commandExecutor, String name, String responseQueueName,
                            Codec codec, String executorId, BaseRemoteService remoteService) {
        super(commandExecutor, name, responseQueueName, codec, executorId, remoteService);
    }

    /** 创建指定接口的同步远程代理实例。 */
    public <T> T create(Class<T> remoteInterface, RemoteInvocationOptions options) {
        // 复制选项，防止调用方后续修改影响本次调用
        RemoteInvocationOptions optionsCopy = new RemoteInvocationOptions(options);
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("toString")) {
                return proxy.getClass().getName() + "-" + remoteInterface.getName();
            } else if (method.getName().equals("equals")) {
                return proxy == args[0];
            } else if (method.getName().equals("hashCode")) {
                return (proxy.getClass().getName() + "-" + remoteInterface.getName()).hashCode();
            }

            // noResult 模式仅允许 void 返回类型
            if (!optionsCopy.isResultExpected()
                    && !(method.getReturnType().equals(Void.class) || method.getReturnType().equals(Void.TYPE))) {
                throw new IllegalArgumentException("The noResult option only supports void return value");
            }

            // 生成请求 ID 并确定目标请求队列
            String requestId = remoteService.generateRequestId(args);
            String requestQueueName = getRequestQueueName(remoteInterface);
            RemoteServiceRequest request = new RemoteServiceRequest(executorId, requestId, method.getName(),
                                                    remoteService.getMethodSignature(method), args, optionsCopy, System.currentTimeMillis());

            // 若需要 ACK，预先注册 ACK 轮询 Future
            CompletableFuture<RemoteServiceAck> ackFuture;
            if (optionsCopy.isAckExpected()) {
                ackFuture = pollResponse(optionsCopy.getAckTimeoutInMillis(), requestId, false);
            } else {
                ackFuture = null;
            }

            // 若需要返回值，注册响应轮询 Future
            CompletableFuture<RRemoteServiceResponse> responseFuture;
            if (optionsCopy.isResultExpected()) {
                long timeout = remoteService.getTimeout(optionsCopy.getExecutionTimeoutInMillis(), request);
                responseFuture = pollResponse(timeout, requestId, false);
            } else {
                responseFuture = null;
            }

            // 将请求异步写入 Redis 队列
            RemotePromise<Object> addPromise = new RemotePromise<Object>(requestId);
            CompletableFuture<Boolean> futureAdd = remoteService.addAsync(requestQueueName, request, addPromise);
            Boolean res;
            try {
                res = futureAdd.join();
            } catch (Exception e) {
                if (responseFuture != null) {
                    responseFuture.cancel(false);
                }
                if (ackFuture != null) {
                    ackFuture.cancel(false);
                }
                throw e.getCause();
            }

            // 入队失败：取消已注册的轮询并抛错
            if (!res) {
                if (responseFuture != null) {
                    responseFuture.cancel(false);
                }
                if (ackFuture != null) {
                    ackFuture.cancel(false);
                }
                throw new RedisException("Task hasn't been added");
            }

            // 仅在需要 ACK 时阻塞等待确认
            if (ackFuture != null) {
                String ackName = remoteService.getAckName(requestId);
                RemoteServiceAck ack = null;
                try {
                    ack = ackFuture.toCompletableFuture().get(optionsCopy.getAckTimeoutInMillis(), TimeUnit.MILLISECONDS);
                } catch (ExecutionException | TimeoutException e) {
                    // 首次 ACK 超时，稍后重试
                }
                if (ack == null) {
                    CompletionStage<RemoteServiceAck> ackFutureAttempt =
                            tryPollAckAgainAsync(optionsCopy, ackName, requestId);
                    try {
                        ack = ackFutureAttempt.toCompletableFuture().get(optionsCopy.getAckTimeoutInMillis(), TimeUnit.MILLISECONDS);
                    } catch (ExecutionException | TimeoutException e) {
                        // skip
                    }
                    // 重试仍无 ACK 则抛出 ACK 超时异常
                    if (ack == null) {
                        throw new RemoteServiceAckTimeoutException("No ACK response after "
                                + optionsCopy.getAckTimeoutInMillis() + "ms for request: " + request);
                    }
                }
                // ACK 成功后删除 Redis 中的 ACK 键
                new RedissonBucket<>(commandExecutor, ackName).delete();
            }

            // 仅在需要结果时阻塞等待 RemoteServiceResponse
            if (responseFuture != null) {
                RemoteServiceResponse response = null;
                try {
                    response = (RemoteServiceResponse) responseFuture.toCompletableFuture().join();
                } catch (Exception e) {
                    // skip
                }
                // 响应超时
                if (response == null) {
                    throw new RemoteServiceTimeoutException("No response after "
                            + optionsCopy.getExecutionTimeoutInMillis() + "ms for request: " + request);
                }
                // 服务端执行异常原样抛出
                if (response.getError() != null) {
                    throw response.getError();
                }
                // Optional 返回类型：null 映射为 empty
                if (method.getReturnType().equals(Optional.class)) {
                    if (response.getResult() == null) {
                        return Optional.empty();
                    }
                    return Optional.of(response.getResult());
                }
                return response.getResult();
            }

            return null;
        };
        return (T) Proxy.newProxyInstance(remoteInterface.getClassLoader(), new Class[] { remoteInterface }, handler);
    }

}
