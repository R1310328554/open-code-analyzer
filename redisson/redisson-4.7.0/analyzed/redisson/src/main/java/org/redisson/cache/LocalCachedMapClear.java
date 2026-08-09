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
 * 本地缓存清空广播消息。
 * <p>
 * 由发起清空的实例发布，其他实例收到后清空本地缓存。
 *
 * @author Nikita Koksharov
 *
 */
@SuppressWarnings("serial")
public class LocalCachedMapClear implements Serializable {

    /** 发起清空操作的实例 ID（接收方需排除自身）。 */
    private byte[] excludedId;
    /** 清空请求 ID，用于信号量协调。 */
    private byte[] requestId;
    /** 接收方处理完成后是否释放信号量。 */
    private boolean releaseSemaphore;

    public LocalCachedMapClear() {
    }
    
    public LocalCachedMapClear(byte[] excludedId, byte[] requestId, boolean releaseSemaphore) {
        this.excludedId = excludedId;
        this.requestId = requestId;
        this.releaseSemaphore = releaseSemaphore;
    }

    public byte[] getExcludedId() {
        return excludedId;
    }

    public boolean isReleaseSemaphore() {
        return releaseSemaphore;
    }

    public byte[] getRequestId() {
        return requestId;
    }
    
}
