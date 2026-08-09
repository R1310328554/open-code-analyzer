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
 * 本地缓存禁用广播消息。
 * <p>
 * 可禁用指定键或整个本地缓存，并在超时后自动恢复。
 *
 * @author Nikita Koksharov
 *
 */
public class LocalCachedMapDisable implements Serializable {

    /** 待禁用键的哈希数组。 */
    private byte[][] keyHashes;
    /** 禁用持续时间（毫秒）。 */
    private long timeout;
    /** 发起禁用请求的唯一标识。 */
    private String requestId;
    /** 是否禁用整个本地缓存（而非仅指定键）。 */
    private boolean disableCache;

    public boolean isDisableCache() {
        return disableCache;
    }
    
    public LocalCachedMapDisable() {
    }

    public LocalCachedMapDisable(String requestId, byte[][] keyHashes, long timeout, boolean disableCache) {
        super();
        this.requestId = requestId;
        this.keyHashes = keyHashes;
        this.timeout = timeout;
        this.disableCache = disableCache;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public byte[][] getKeyHashes() {
        return keyHashes;
    }
    
}
