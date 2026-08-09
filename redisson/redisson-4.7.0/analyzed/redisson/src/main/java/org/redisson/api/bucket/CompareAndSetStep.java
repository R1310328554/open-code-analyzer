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
package org.redisson.api.bucket;

/**
 * compare-and-set 操作的中间构建器接口。
 * <p>
 * 由条件工厂方法返回，必须继续调用 {@link #set(Object)} 指定新值。
 *
 * @author Nikita Koksharov
 *
 * @param <V> 值类型
 */
public interface CompareAndSetStep<V> {

    /**
     * 设置条件满足时要存储的新值（必填）。
     *
     * @param value 新值
     * @return {@link CompareAndSetArgs}，可继续配置 TTL 或过期时间
     */
    CompareAndSetArgs<V> set(V value);

}