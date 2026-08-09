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
 * 远程服务响应消息的公共接口。
 * <p>
 * 实现类包括 {@link RemoteServiceAck}、{@link RemoteServiceResponse}、
 * {@link RemoteServiceCancelResponse} 等，均通过响应队列回传并携带 requestId。
 *
 * @author Nikita Koksharov
 *
 */
public interface RRemoteServiceResponse extends Serializable {

    /** @return 与 {@link RemoteServiceRequest} 对应的请求 ID */
    String getId();
    
}
