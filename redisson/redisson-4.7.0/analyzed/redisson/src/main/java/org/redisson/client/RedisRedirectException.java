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
package org.redisson.client;

import org.redisson.misc.RedisURI;

/**
 * Redis 集群 MOVED 重定向异常基类。
 * <p>
 * 携带目标槽位（可选）与节点 {@link RedisURI}，供客户端跟随重定向。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisRedirectException extends RedisException {

    private static final long serialVersionUID = 181505625075250011L;

    /** 重定向目标槽位，未知时为 {@code null}。 */
    private final Integer slot;
    /** 应连接的目标 Redis 节点 URI。 */
    private final RedisURI url;

    /** 仅指定目标节点 URI 构造重定向异常。 */
    public RedisRedirectException(RedisURI url) {
        slot = null;
        this.url = url;
    }

    /** 指定槽位与目标节点 URI 构造重定向异常。 */
    public RedisRedirectException(Integer slot, RedisURI url) {
        this.slot = slot;
        this.url = url;
    }

    /** 返回重定向涉及的哈希槽位。 */
    public Integer getSlot() {
        return slot;
    }

    /** 返回客户端应连接的目标节点 URI。 */
    public RedisURI getUrl() {
        return url;
    }

}
