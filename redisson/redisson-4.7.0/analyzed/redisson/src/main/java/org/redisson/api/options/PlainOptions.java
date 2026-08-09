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
 * 普通 Redis 对象实例的选项配置。
 *
 * @author Nikita Koksharov
 *
 */
public interface PlainOptions extends CodecOptions<PlainOptions, Codec>, ReadModeOptions<PlainOptions> {

    /**
     * 按对象实例名称创建选项。
     *
     * @param name 对象实例名称
     * @return 选项实例
     */
    static PlainOptions name(String name) {
        return new PlainParams(name);
    }

}
