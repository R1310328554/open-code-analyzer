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

/**
 * 集群或哨兵拓扑中找不到目标 Redis 节点时抛出。
 * <p>
 * 可能因节点下线、槽位映射过期或配置不一致导致。
 *
 * @author Nikita Koksharov
 *
 */
public class RedisNodeNotFoundException extends RedisException {

    private static final long serialVersionUID = -4756928186967834601L;

    /** 使用错误消息构造节点未找到异常。 */
    public RedisNodeNotFoundException(String msg) {
        super(msg);
    }

}
