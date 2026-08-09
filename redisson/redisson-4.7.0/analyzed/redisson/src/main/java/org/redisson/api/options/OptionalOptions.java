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
package org.redisson.api.options;

import org.redisson.client.codec.Codec;

/**
 * 可选分布式对象的通用配置选项（编解码器等）。
 *
 * @author Nikita Koksharov
 *
 */
public interface OptionalOptions extends CodecOptions<OptionalOptions, Codec> {

    /**
     * 创建默认配置。
     *
     * @return 配置实例
     */
    static OptionalOptions defaults() {
        return new OptionalParams();
    }

}
