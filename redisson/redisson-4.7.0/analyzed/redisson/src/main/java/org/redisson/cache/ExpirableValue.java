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

/**
 * 可过期值接口。
 * <p>
 * 缓存条目及嵌套值可实现此接口以支持 TTL 判断。
 *
 * @author Nikita Koksharov
 *
 */
public interface ExpirableValue {

    /** 判断值是否已过期。 */
    boolean isExpired();

    /** 返回过期时间戳（毫秒）。 */
    long getExpireTime();
}
