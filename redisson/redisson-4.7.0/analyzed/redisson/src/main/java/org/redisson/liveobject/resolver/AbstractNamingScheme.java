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
package org.redisson.liveobject.resolver;

import org.redisson.client.codec.Codec;

/**
 * {@link NamingScheme} 抽象基类，持有构造时注入的 {@link Codec}。
 * <p>
 * 具体命名规则由 {@link DefaultNamingScheme} 等子类实现。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
public abstract class AbstractNamingScheme implements NamingScheme {
    
    /** 用于 id 编解码的 Codec，与 Live Object Redis key 一致。 */
    protected final Codec codec;

    /** @param codec Live Object 使用的序列化 Codec */
    public AbstractNamingScheme(Codec codec) {
        this.codec = codec;
    }
    
    /** 返回构造时绑定的 Codec。 */
    @Override
    public Codec getCodec() {
        return codec;
    }
    
}
