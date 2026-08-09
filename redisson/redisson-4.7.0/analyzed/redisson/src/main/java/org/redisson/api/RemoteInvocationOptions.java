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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * {@link RRemoteService} 远程调用选项；用于配置远程调用的 ACK 确认超时与执行结果超时。
 * <p>
 * Examples:
 * <pre>
 *     // 1 second ack timeout and 30 seconds execution timeout
 *     RemoteInvocationOptions options =
 *          RemoteInvocationOptions.defaults();
 *
 *     // no ack but 30 seconds execution timeout
 *     RemoteInvocationOptions options =
 *          RemoteInvocationOptions.defaults()
 *              .noAck();
 *
 *     // 1 second ack timeout then forget the result
 *     RemoteInvocationOptions options =
 *          RemoteInvocationOptions.defaults()
 *              .noResult();
 *
 *     // 1 minute ack timeout then forget about the result
 *     RemoteInvocationOptions options =
 *          RemoteInvocationOptions.defaults()
 *              .expectAckWithin(1, TimeUnit.MINUTES)
 *              .noResult();
 *
 *     // no ack and forget about the result (fire and forget)
 *     RemoteInvocationOptions options =
 *          RemoteInvocationOptions.defaults()
 *              .noAck()
 *              .noResult();
 * </pre>
 *
 * @see RRemoteService#get(Class, RemoteInvocationOptions)
 */
public final class RemoteInvocationOptions implements Serializable {

    private static final long serialVersionUID = -7715968073286484802L;
    
    private Long ackTimeoutInMillis;
    private Long executionTimeoutInMillis;

    private RemoteInvocationOptions() {
    }

    public RemoteInvocationOptions(RemoteInvocationOptions copy) {
        this.ackTimeoutInMillis = copy.ackTimeoutInMillis;
        this.executionTimeoutInMillis = copy.executionTimeoutInMillis;
    }

    /**
     * 创建带默认配置的 {@link RemoteInvocationOptions} 实例。
     * <p>
     * 等价于：
     * <pre>
     *     new RemoteInvocationOptions()
     *      .expectAckWithin(1, TimeUnit.SECONDS)
     *      .expectResultWithin(30, TimeUnit.SECONDS)
     * </pre>
     * 
     * @return 远程调用选项实例
     */
    public static RemoteInvocationOptions defaults() {
        return new RemoteInvocationOptions()
                .expectAckWithin(1, TimeUnit.SECONDS)
                .expectResultWithin(30, TimeUnit.SECONDS);
    }

    public Long getAckTimeoutInMillis() {
        return ackTimeoutInMillis;
    }

    public Long getExecutionTimeoutInMillis() {
        return executionTimeoutInMillis;
    }

    public boolean isAckExpected() {
        return ackTimeoutInMillis != null;
    }

    public boolean isResultExpected() {
        return executionTimeoutInMillis != null;
    }

    /**
     * 设置 ACK 确认超时。
     * 
     * @param ackTimeoutInMillis ACK 超时（毫秒）
     * @return 远程调用选项实例
     */
    public RemoteInvocationOptions expectAckWithin(long ackTimeoutInMillis) {
        this.ackTimeoutInMillis = ackTimeoutInMillis;
        return this;
    }

    /**
     * 设置 ACK 确认超时。
     * 
     * @param ackTimeout ACK 超时数值
     * @param timeUnit 时间单位
     * @return 远程调用选项实例
     */
    public RemoteInvocationOptions expectAckWithin(long ackTimeout, TimeUnit timeUnit) {
        this.ackTimeoutInMillis = timeUnit.toMillis(ackTimeout);
        return this;
    }

    /**
     * 不等待 ACK 确认（即发即忘模式之一）。
     * 
     * @return 远程调用选项实例
     */
    public RemoteInvocationOptions noAck() {
        ackTimeoutInMillis = null;
        return this;
    }

    /**
     * 设置远程方法执行结果超时。
     * 
     * @param executionTimeoutInMillis 执行超时（毫秒）
     * @return 远程调用选项实例
     */
    public RemoteInvocationOptions expectResultWithin(long executionTimeoutInMillis) {
        this.executionTimeoutInMillis = executionTimeoutInMillis;
        return this;
    }

    /**
     * 设置远程方法执行结果超时。
     * 
     * @param executionTimeout 执行超时数值
     * @param timeUnit 时间单位
     * @return 远程调用选项实例
     */
    public RemoteInvocationOptions expectResultWithin(long executionTimeout, TimeUnit timeUnit) {
        this.executionTimeoutInMillis = timeUnit.toMillis(executionTimeout);
        return this;
    }

    /**
     * 不等待执行结果（即发即忘模式之一）。
     * 
     * @return 远程调用选项实例
     */
    public RemoteInvocationOptions noResult() {
        executionTimeoutInMillis = null;
        return this;
    }

    @Override
    public String toString() {
        return "RemoteInvocationOptions[" +
                "ackTimeoutInMillis=" + ackTimeoutInMillis +
                ", executionTimeoutInMillis=" + executionTimeoutInMillis +
                ']';
    }
}
