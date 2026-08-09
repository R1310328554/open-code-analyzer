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
 * Worker 确认任务已取消的响应消息，实现 {@link RRemoteServiceResponse}。
 * <p>
 * {@link #isCanceled()} 为 true 表示取消成功；客户端收到后完成
 * {@link org.redisson.executor.RemotePromise} 的 cancel 流程。
 * 
 * @author Nikita Koksharov
 *
 */
public class RemoteServiceCancelResponse implements RRemoteServiceResponse, Serializable {

    private static final long serialVersionUID = -4356901222132702182L;

    /** 请求 ID。 */
    private String id;
    /** 是否已成功取消。 */

    public RemoteServiceCancelResponse() {
    }
    
    /** @param id 请求 ID @param canceled 取消是否生效 */
    public RemoteServiceCancelResponse(String id, boolean canceled) {
        this.canceled = canceled;
        this.id = id;
    }
    
    /** @return 取消结果 */
    public boolean isCanceled() {
        return canceled;
    }

    /** @return 请求 ID */
    @Override
    public String getId() {
        return id;
    }
    
}
