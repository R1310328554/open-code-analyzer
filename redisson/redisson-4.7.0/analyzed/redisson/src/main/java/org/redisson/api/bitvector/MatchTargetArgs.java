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

/**
 * 构建 {@link MatchExactArgs} 的中间阶段；由 {@link MatchExactArgs#mask(long)} 产生，
 * 经 {@link #target(long)} 完成并得到可用于 {@link org.redisson.api.RBitVectorStore#matchExact(MatchExactArgs)} 的完整参数。
 * <p>
 * 分阶段构建在编译期强制精确匹配查询必须同时提供掩码与目标值。
 *
 * @see MatchExactArgs
 * @author Nikita Koksharov
 */
public interface MatchTargetArgs {

    /**
     * 设置掩码选定比特位上须匹配的目标比特模式；完整谓词为 {@code (vector & mask) == target}。
     * <p>
     * 若 {@code value} 在先前提供的掩码外有置位比特，则谓词不可满足，后续查询结果为空。
     *
     * @param value 掩码范围内的目标比特模式
     * @return 可立即使用或继续配置的完整 {@link MatchExactArgs}
     */
    MatchExactArgs target(long value);

}