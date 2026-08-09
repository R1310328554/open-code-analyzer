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
package org.redisson.cache;

import java.io.Serializable;

/**
 * 已禁用键或缓存的持久化记录。
 * <p>
 * 存储在 Redis 中，实例启动时恢复禁用状态。
 *
 * @author Nikita Koksharov
 *
 */
public class LocalCachedMapDisabledKey implements Serializable {

    /** 禁用请求的唯一标识。 */
    private String requestId;
    /** 禁用剩余时间（毫秒）。 */
    private long timeout;
    
    public LocalCachedMapDisabledKey() {
    }
    
    public LocalCachedMapDisabledKey(String requestId, long timeout) {
        super();
        this.requestId = requestId;
        this.timeout = timeout;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
}
