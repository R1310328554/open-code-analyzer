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
package org.redisson.api.cuckoofilter;

import java.util.Collection;

/**
 * 布谷鸟过滤器（Cuckoo Filter）批量添加元素的参数接口。
 *
 * <p>用法示例：
 * <pre>
 *     Set&lt;String&gt; added = filter.add(
 *         CuckooFilterAddArgs.&lt;String&gt;items(List.of("a", "b", "c"))
 *                 .capacity(50000)
 *                 .noCreate());
 * </pre>
 *
 * @param <V> 元素类型
 *
 * @author Nikita Koksharov
 *
 */
public interface CuckooFilterAddArgs<V> {

    /**
     * 创建包含待插入元素集合的参数对象。
     *
     * @param items 待插入元素
     * @param <V> 元素类型
     * @return 参数实例
     */
    static <V> CuckooFilterAddArgs<V> items(Collection<V> items) {
        return new CuckooFilterAddArgsImpl<>(items);
    }

    /**
     * 设置命令自动创建过滤器时的期望容量。
     *
     * @param capacity 自动创建时的过滤器容量
     * @return 参数实例
     */
    CuckooFilterAddArgs<V> capacity(long capacity);

    /**
     * 禁止自动创建过滤器；若过滤器不存在则命令失败。
     *
     * @return 参数实例
     */
    CuckooFilterAddArgs<V> noCreate();

}
