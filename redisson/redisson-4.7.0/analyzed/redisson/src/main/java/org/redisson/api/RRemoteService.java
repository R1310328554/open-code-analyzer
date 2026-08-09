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
package org.redisson.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 支持 Redisson 实例之间远程调用对象方法（服务端 Worker 与客户端调用方）。
 * <p>
 * <b>1. 服务端（Worker 实例）。</b> 向 {@link RRemoteService} 注册实现对象。
 * <p>
 * <code>
 * RRemoteService remoteService = redisson.getRemoteService();<br>
 * <br>
 * // register remote service before any remote invocation<br>
 * remoteService.register(SomeServiceInterface.class, someServiceImpl);
 * </code>
 * <p>
 * <b>2. 客户端。</b> 通过动态代理远程调用方法。
 * <p>
 * <code>
 * RRemoteService remoteService = redisson.getRemoteService();<br>
 * SomeServiceInterface service = remoteService.get(SomeServiceInterface.class);<br>
 * <br>
 * String result = service.doSomeStuff(1L, "secondParam", new AnyParam());
 * </code>
 * <p>
 * 执行过程中涉及两类超时：
 * <p>
 * <b>确认（Ack）超时。</b> 客户端等待服务端确认消息。
 * <p>
 * 若客户端未收到确认则抛出 <code>RemoteServiceAckTimeoutException</code>，
 * 可重试下一次调用。
 * <p>
 * 若客户端未收到确认但服务端已收到调用消息，
 * 服务端会因 Ack 超时而跳过该次执行。
 * <p>
 * <b>执行超时。</b> 客户端已收到确认；若在执行超时内未收到结果或错误，
 * 则抛出 <code>RemoteServiceTimeoutException</code>。
 * 
 * @author Nikita Koksharov
 *
 */
public interface RRemoteService {

    /**
     * 返回可用于处理远程调用的空闲 Worker 数量
     * 
     * @param remoteInterface - remote service interface
     * @return Worker 数量
     */
    int getFreeWorkers(Class<?> remoteInterface);
    
    /**
     * 返回等待空闲 Worker 处理的待执行远程调用数量
     * 
     * @param remoteInterface - remote service interface
     * @return 待处理调用数量
     */
    int getPendingInvocations(Class<?> remoteInterface);

    /**
     * Returns pending invocations amount for handling in free workers.
     *
     * @param remoteInterface - remote service interface
     * @return invocations amount
     */
    RFuture<Integer> getPendingInvocationsAsync(Class<?> remoteInterface);

    /**
     * 注册远程服务，使用单个 Worker 处理调用
     *
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     */
    <T> void register(Class<T> remoteInterface, T object);
    
    /**
     * Register remote service with custom workers amount
     *
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     * @param workersAmount - workers amount
     */
    <T> void register(Class<T> remoteInterface, T object, int workersAmount);

    /**
     * Register remote service with custom workers amount
     * and executor for running them
     * 
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     * @param workers - workers amount
     * @param executor - executor service used to invoke methods
     */
    <T> void register(Class<T> remoteInterface, T object, int workers, ExecutorService executor);
    
    /**
     * 注销指定远程服务的全部 Worker
     *
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     */
    <T> void deregister(Class<T> remoteInterface);

    /**
     * Tries to execute one awaiting remote request.
     * Waits up to <code>timeout</code> if necessary until remote request became available.
     *
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     * @param timeout - maximum wait time until remote request became available
     * @param timeUnit - time unit
     * @param <T> - type of remote service
     * @return <code>true</code> if method was successfully executed and
     *          <code>false</code> if timeout reached before execution
     * @throws InterruptedException - if the thread is interrupted
     */
    <T> boolean tryExecute(Class<T> remoteInterface, T object, long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Tries to execute one awaiting remote request.
     * Waits up to <code>timeout</code> if necessary until remote request became available.
     *
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     * @param timeout - maximum wait time until remote request became available
     * @param timeUnit - time unit
     * @param executorService - executor service used to invoke methods
     * @param <T> - type of remote service
     * @return <code>true</code> if method was successfully executed and
     *          <code>false</code> if timeout reached before execution
     * @throws InterruptedException - if the thread is interrupted
     */
    <T> boolean tryExecute(Class<T> remoteInterface, T object, ExecutorService executorService, long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Tries to execute one awaiting remote request.
     *
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     * @param <T> - type of remote service
     * @return <code>true</code> if method was successfully executed and
     *          <code>false</code> if timeout reached before execution
     */
    <T> RFuture<Boolean> tryExecuteAsync(Class<T> remoteInterface, T object);

    /**
     * Tries to execute one awaiting remote request.
     * Waits up to <code>timeout</code> if necessary until remote request became available.
     *
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     * @param timeout - maximum wait time until remote request became available
     * @param timeUnit - time unit
     * @param <T> - type of remote service
     * @return <code>true</code> if method was successfully executed and
     *          <code>false</code> if timeout reached before execution
     */
    <T> RFuture<Boolean> tryExecuteAsync(Class<T> remoteInterface, T object, long timeout, TimeUnit timeUnit);

    /**
     * Tries to execute one awaiting remote request.
     * Waits up to <code>timeout</code> if necessary until remote request became available.
     *
     * @param remoteInterface - remote service interface
     * @param object - remote service object
     * @param timeout - maximum wait time until remote request became available
     * @param timeUnit - time unit
     * @param executorService - executor service used to invoke methods
     * @param <T> - type of remote service
     * @return <code>true</code> if method was successfully executed and
     *          <code>false</code> if timeout reached before execution
     */
    <T> RFuture<Boolean> tryExecuteAsync(Class<T> remoteInterface, T object, ExecutorService executorService, long timeout, TimeUnit timeUnit);

    /**
     * 获取用于远程调用的动态代理对象。
     * <p>
     * This method is a shortcut for
     * <pre>
     *     get(remoteInterface, RemoteInvocationOptions.defaults())
     * </pre>
     *
     * @see RemoteInvocationOptions#defaults()
     * @see #get(Class, RemoteInvocationOptions)
     *
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     * @return 远程服务代理实例
     */
    <T> T get(Class<T> remoteInterface);

    /**
     * Get remote service object for remote invocations 
     * with specified invocation timeout.
     * <p>
     * This method is a shortcut for
     * <pre>
     *     get(remoteInterface, RemoteInvocationOptions.defaults()
     *      .expectResultWithin(executionTimeout, executionTimeUnit))
     * </pre>
     *
     * @see RemoteInvocationOptions#defaults()
     * @see #get(Class, RemoteInvocationOptions)
     *
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     * @param executionTimeout - invocation timeout
     * @param executionTimeUnit - time unit
     * @return remote service instance
     */
    <T> T get(Class<T> remoteInterface, long executionTimeout, TimeUnit executionTimeUnit);
    
    /**
     * Get remote service object for remote invocations
     * with specified invocation and ack timeouts
     * <p>
     * This method is a shortcut for
     * <pre>
     *     get(remoteInterface, RemoteInvocationOptions.defaults()
     *      .expectAckWithin(ackTimeout, ackTimeUnit)
     *      .expectResultWithin(executionTimeout, executionTimeUnit))
     * </pre>
     *
     * @see RemoteInvocationOptions
     * @see #get(Class, RemoteInvocationOptions)
     * 
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     * @param executionTimeout - invocation timeout
     * @param executionTimeUnit - time unit
     * @param ackTimeout - ack timeout
     * @param ackTimeUnit - time unit
     * @return remote service object
     */
    <T> T get(Class<T> remoteInterface, long executionTimeout, TimeUnit executionTimeUnit, long ackTimeout, TimeUnit ackTimeUnit);

    /**
     * Get remote service object for remote invocations
     * with the specified options
     * <p>
     * Note that when using the noResult() option,
     * it is expected that the invoked method returns void,
     * or else IllegalArgumentException will be thrown.
     *
     * @see RemoteInvocationOptions
     * 
     * @param <T> type of remote service
     * @param remoteInterface - remote service interface
     * @param options - service options
     * @return remote service object
     */
    <T> T get(Class<T> remoteInterface, RemoteInvocationOptions options);

}
