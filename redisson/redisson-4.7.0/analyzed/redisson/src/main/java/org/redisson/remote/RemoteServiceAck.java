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

/**
 * Worker 收到 {@link RemoteServiceRequest} 后发送的 ACK 消息。
 * <p>
 * 表示远程方法已开始执行（或至少已被 Worker 取走）；
 * 客户端在 {@link RemoteInvocationOptions#isAckExpected()} 为 true 时等待此消息。
 * 
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceAck implements RRemoteServiceResponse, Serializable {

    private static final long serialVersionUID = -6332680404562746984L;

    /** 对应的请求 ID。 */

    public RemoteServiceAck() {
    }

    /** @param id 请求 ID */
    public RemoteServiceAck(String id) {
        this.id = id;
    }
    
    /** @return 请求 ID */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "RemoteServiceAck [id=" + id + "]";
    }
    
}
