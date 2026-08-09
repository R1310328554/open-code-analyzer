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

/**
 * 布谷鸟过滤器初始化参数接口。
 *
 * <p>用法示例：
 * <pre>
 *     filter.init(CuckooFilterInitArgs.capacity(100000)
 *                     .bucketSize(4)
 *                     .maxIterations(500)
 *                     .expansion(2));
 * </pre>
 *
 * @author Nikita Koksharov
 *
 */
public interface CuckooFilterInitArgs {

    /**
     * 创建指定容量的初始化参数。
     *
     * @param capacity 过滤器预期存储的元素数量
     * @return 参数实例
     */
    static CuckooFilterInitArgs capacity(long capacity) {
        return new CuckooFilterInitArgsImpl(capacity);
    }

    /**
     * 设置每个桶可容纳的元素数量。
     * <p>
     * 默认值为 2；桶越大填充率越高，但误判率也可能上升。
     *
     * @param bucketSize 每桶元素数
     * @return 参数实例
     */
    CuckooFilterInitArgs bucketSize(long bucketSize);

    /**
     * 设置判定过滤器已满前，桶间交换元素的最大尝试次数。
     * <p>
     * 默认值为 20。
     *
     * @param maxIterations 最大交换尝试次数
     * @return 参数实例
     */
    CuckooFilterInitArgs maxIterations(long maxIterations);

    /**
     * 设置过滤器满时的扩容倍率。
     * <p>
     * 默认值为 1。
     *
     * @param expansion 扩容倍率
     * @return 参数实例
     */
    CuckooFilterInitArgs expansion(long expansion);

}
