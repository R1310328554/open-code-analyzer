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

import java.io.Serializable;
import java.util.Arrays;

import org.redisson.api.RemoteInvocationOptions;

/**
 * 远程 RPC 请求体（可序列化）：
 * 客户端通过 Redis 队列发送，服务端 {@link BaseRemoteService} 反序列化后
 * 按 {@link #methodName} 与 {@link #signature} 定位本地方法并执行。
 * <p>
 * 包含执行器 ID、调用选项（ACK/超时等）与时间戳。
 *
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceRequest implements Serializable {

    private static final long serialVersionUID = -1711385312384040075L;

    /** 请求唯一 ID，用于匹配响应与 ACK。 */
    private String id;
    /** 目标执行器标识。 */
    private String executorId;
    /** 远程方法名。 */
    private String methodName;
    /** 方法参数类型签名。 */
    private long[] signature;
    /** 实际调用参数数组。 */
    private Object[] args;
    /** 调用选项（是否等待 ACK/结果、超时等）。 */
    private RemoteInvocationOptions options;
    /** 请求创建时间戳（毫秒）。 */
    private long date;
    
    
    /** 无参构造，供序列化框架使用。 */
    public RemoteServiceRequest() {
    }
    
    /** @param id 仅指定请求 ID 的简化构造 */
    public RemoteServiceRequest(String id) {
        this.id = id;
    }
    
    /** 完整构造：携带执行器、方法、参数与选项。 */
    public RemoteServiceRequest(String executorId, String id, String methodName, long[] signature, Object[] args, RemoteInvocationOptions options, long date) {
        super();
        this.id = id;
        this.executorId = executorId;
        this.methodName = methodName;
        this.signature = signature;
        this.args = args;
        this.options = options;
        this.date = date;
    }
    
    /** @return 请求时间戳 */
    public long getDate() {
        return date;
    }
    
    /** @return 执行器 ID */
    public String getExecutorId() {
        return executorId;
    }
    
    /** @return 请求 ID */
    public String getId() {
        return id;
    }

    /** @return 调用参数数组 */
    public Object[] getArgs() {
        return args;
    }

    /** @return 方法签名数组 */
    public long[] getSignature() {
        return signature;
    }
    
    /** @return 远程调用选项 */
    public RemoteInvocationOptions getOptions() {
        return options;
    }

    /** @return 方法名 */
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "RemoteServiceRequest [requestId=" + id + ", methodName=" + methodName + ", signature="
                + Arrays.toString(signature) + ", args="
                + Arrays.toString(args) + ", options=" + options + ", date=" + date + "]";
    }

}
