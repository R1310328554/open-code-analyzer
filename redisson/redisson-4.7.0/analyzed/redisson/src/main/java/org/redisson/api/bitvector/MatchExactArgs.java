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
package org.redisson.api.bitvector;

import java.time.Duration;

/**
 * {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs) matchExact} 精确匹配查询的参数构建器。
 * <p>
 * 携带必需的掩码（mask）与目标值（target），以及控制服务端分批迭代结果的可选调优参数。
 * 精确匹配谓词为 {@code (vector & mask) == target}：掩码外比特被忽略，掩码内比特须与 {@code target} 对应位相等。
 * <pre>{@code
 *   MatchExactArgs args = MatchExactArgs.mask(0b101001L)
 *                                       .target(0b100001L)
 *                                       .chunkSize(2048)
 *                                       .chunkFetchTTL(Duration.ofMinutes(2));
 * }</pre>
 * <p>
 * 若 {@code target} 在 {@code mask} 外有置位比特，则谓词不可满足，查询结果为空。
 *
 * @see MatchArgs
 * @see MatchTargetArgs
 * @author Nikita Koksharov
 */
public interface MatchExactArgs {

    /**
     * 以位掩码开始构建；掩码选定参与相等比较的比特位，掩码外比特在匹配时被忽略。
     * <p>
     * 返回 {@link MatchTargetArgs} 阶段，须调用 {@link MatchTargetArgs#target(long)} 得到可用的 {@code MatchExactArgs}。
     *
     * @param value 位掩码
     * @return 等待目标值的下一构建阶段
     */
    static MatchTargetArgs mask(long value) {
        return new MatchExactParams(value);
    }

    /**
     * 设置结果迭代时每次服务端往返拉取的键数量（批次大小）。
     *
     * @param value 批次大小，须为正数
     * @return 当前构建器，支持链式调用
     */
    MatchExactArgs chunkSize(int value);

    /**
     * 设置查询创建的服务端迭代状态的生存时间（TTL）。
     * 若调用方未完整消费迭代器或 JVM 中途退出，TTL 到期后服务端状态将被自动回收。
     *
     * @param value 应用于服务端迭代状态的 TTL
     * @return 当前构建器，支持链式调用
     */
    MatchExactArgs chunkFetchTTL(Duration value);

}