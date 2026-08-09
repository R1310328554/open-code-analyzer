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
package org.redisson.api.bloomfilter;

/**
 * {@code BF.RESERVE} 命令初始化参数构建器入口；通过 {@link #create()} 开始链式配置。
 *
 * @author Su Ko
 */
public interface BloomFilterInitArgs {

    /**
     * 创建 {@code BF.RESERVE} 参数构建流程，下一步设置期望误判率。
     *
     * @return 误判率配置阶段
     */
    static ErrorRateBloomFilterInitArgs create(){
        return new BloomFilterInitParams();
    }
}
