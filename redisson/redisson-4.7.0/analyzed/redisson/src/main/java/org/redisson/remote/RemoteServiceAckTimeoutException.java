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

/**
 * 在 ACK 超时时间内 Worker 未确认收到请求时抛出。
 * <p>
 * 此时远程方法尚未开始执行，客户端可以安全重试调用。
 * 
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceAckTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 1820133675653636587L;

    /** @param message 超时详情（含 requestId 与毫秒数） */
    public RemoteServiceAckTimeoutException(String message) {
        super(message);
    }
    
}
