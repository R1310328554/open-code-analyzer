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
package org.redisson.executor.params;

import java.io.Serializable;

/**
 * 远程执行器任务的序列化参数载体，可在 Redis 与 Worker 间传输。
 * <p>
 * 包含任务类字节码、实例状态、lambda 序列化体及 TTL 等元数据。
 *
 * @author Nikita Koksharov
 *
 */
public class TaskParameters implements Serializable {

    private static final long serialVersionUID = -5662511632962297898L;
    
    /** 任务类全限定名。 */
    private String className;
    /** 任务类 .class 字节码（用于动态 ClassLoader）。 */
    private byte[] classBody;
    /** 序列化的 lambda 实例字节（非 null 时优先于 state）。 */
    private byte[] lambdaBody;
    /** Codec 编码的任务实例状态字节。 */
    private byte[] state;
    /** 任务唯一请求 ID。 */
    private String requestId;
    /** 任务存活 TTL（毫秒），0 表示不过期。 */
    private long ttl;

    /** 无参构造，供序列化使用。 */
    public TaskParameters() {
    }

    /** @param requestId 任务请求 ID */
    public TaskParameters(String requestId) {
        this.requestId = requestId;
    }

    /** 携带完整序列化载荷构造任务参数。 */
    public TaskParameters(String requestId, String className, byte[] classBody, byte[] lambdaBody, byte[] state) {
        super();
        this.requestId = requestId;
        this.className = className;
        this.classBody = classBody;
        this.state = state;
        this.lambdaBody = lambdaBody;
    }

    /** 返回任务 TTL（毫秒）。 */
    public long getTtl() {
        return ttl;
    }
    /** 设置任务 TTL。 */
    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    /** 返回 lambda 序列化字节，可能为 null。 */
    public byte[] getLambdaBody() {
        return lambdaBody;
    }
    /** 设置 lambda 序列化体。 */
    public void setLambdaBody(byte[] lambdaBody) {
        this.lambdaBody = lambdaBody;
    }

    /** 返回任务类名。 */
    public String getClassName() {
        return className;
    }
    /** 设置任务类全限定名。 */
    public void setClassName(String className) {
        this.className = className;
    }
    
    /** 返回类字节码数组。 */
    public byte[] getClassBody() {
        return classBody;
    }
    /** 设置类字节码。 */
    public void setClassBody(byte[] classBody) {
        this.classBody = classBody;
    }
    
    /** 返回 Codec 编码的实例状态。 */
    public byte[] getState() {
        return state;
    }
    /** 设置实例状态字节。 */
    public void setState(byte[] state) {
        this.state = state;
    }
    
    /** 返回任务请求 ID。 */
    public String getRequestId() {
        return requestId;
    }
    /** 设置任务请求 ID。 */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /** 返回包含 className 与 requestId 的调试字符串。 */
    @Override
    public String toString() {
        return "TaskParameters{" +
                "className='" + className + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
