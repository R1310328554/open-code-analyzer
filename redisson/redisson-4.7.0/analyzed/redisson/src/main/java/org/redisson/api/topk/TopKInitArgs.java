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
package org.redisson.api.topk;

/**
 * Top-K 结构初始化参数对象。
 *
 * <p>用法示例：
 * <pre>
 *     topK.init(TopKInitArgs.topK(50)
 *                     .width(2000)
 *                     .depth(7)
 *                     .decay(0.925));
 * </pre>
 *
 * @author Nikita Koksharov
 *
 */
public interface TopKInitArgs {

    /**
     * 创建跟踪前 {@code topK} 个高频元素的 Top-K 初始化参数。
     *
     * @param topK 需跟踪的高频元素数量
     * @return 参数实例
     */
    static TopKInitArgs topK(int topK) {
        return new TopKInitArgsImpl(topK);
    }

    /**
     * 定义每个计数器数组的宽度（counter 数量）。
     * <p>
     * 默认值为 8。
     *
     * @param width 每个数组中的计数器数量
     * @return 参数实例
     */
    TopKInitArgs width(int width);

    /**
     * 定义计数器数组层数（深度）。
     * <p>
     * 默认值为 7。
     *
     * @param depth 计数器数组数量
     * @return 参数实例
     */
    TopKInitArgs depth(int depth);

    /**
     * 定义碰撞时计数器衰减的概率，取值范围为 {@code (0, 1)}。
     * <p>
     * 默认值为 0.9。
     *
     * @param decay 计数器衰减概率
     * @return 参数实例
     */
    TopKInitArgs decay(double decay);

}
