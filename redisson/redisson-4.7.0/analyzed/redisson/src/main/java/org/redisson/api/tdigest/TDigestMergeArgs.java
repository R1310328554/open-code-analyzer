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
package org.redisson.api.tdigest;

import java.util.Arrays;
import java.util.Collection;

/**
 * {@code TDIGEST.MERGE} 命令的参数对象。
 * <p>
 * 调用合并方法的草图始终为目标；此处提供的键为待合并的源。
 *
 * <p>用法示例：
 * <pre>
 *     destination.mergeWith(
 *         TDigestMergeArgs.keys("server1", "server2")
 *                 .compression(200)
 *                 .override());
 * </pre>
 *
 * @author Nikita Koksharov
 *
 */
public interface TDigestMergeArgs {

    /**
     * 为指定源键创建合并参数。
     *
     * @param keys 源 t-digest 草图的键名
     * @return 参数实例
     */
    static TDigestMergeArgs keys(String... keys) {
        return new TDigestMergeArgsImpl(Arrays.asList(keys));
    }

    /**
     * 为指定源键集合创建合并参数。
     *
     * @param keys 源 t-digest 草图的键名集合
     * @return 参数实例
     */
    static TDigestMergeArgs keys(Collection<String> keys) {
        return new TDigestMergeArgsImpl(keys);
    }

    /**
     * 定义合并后目标草图的压缩率。
     * <p>
     * 若未设置，目标保留当前压缩率；若由本命令创建目标，
     * 则使用各源中的最大压缩率。
     *
     * @param compression 合并结果草图的压缩率
     * @return 参数实例
     */
    TDigestMergeArgs compression(int compression);

    /**
     * 合并前将目标视为空，丢弃其现有观测值并由合并结果覆盖。
     * <p>
     * 未设置此标志时，源数据将叠加到目标当前内容之上。
     *
     * @return 参数实例
     */
    TDigestMergeArgs override();

}
