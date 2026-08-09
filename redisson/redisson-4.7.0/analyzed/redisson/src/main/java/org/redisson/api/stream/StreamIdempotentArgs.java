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
package org.redisson.api.stream;

/**
 * 流幂等写入的参数对象。
 * <p>
 * 支持自动或显式指定幂等 ID，避免重复写入。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamIdempotentArgs<T> {

    /**
     * 启用 IDMPAUTO 模式：Redis 根据消息内容为生产者自动生成唯一幂等 ID。
     * <p>
     * 需要 <b>Redis 8.6.0 及以上版本。</b>
     *
     * @return 参数对象
     */
    T autoId();

    /**
     * 启用 IDMP 模式：为生产者指定显式幂等 ID。
     * <p>
     * 若该生产者与幂等 ID 组合已存在，则返回已有条目 ID 而非创建重复项。
     * <p>
     * 需要 <b>Redis 8.6.0 及以上版本。</b>
     *
     * @param idempotentId 幂等标识
     * @return 参数对象
     */
    T idempotentId(String idempotentId);

}