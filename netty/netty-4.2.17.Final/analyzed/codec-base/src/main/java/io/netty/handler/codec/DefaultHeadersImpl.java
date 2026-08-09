/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec;

import io.netty.util.HashingStrategy;

/**
 * 可直接实例化的 {@link DefaultHeaders} 具体实现。
  * @param <K> header 名称类型。
  * @param <V> header 值类型。
 */
public final class DefaultHeadersImpl<K, V> extends DefaultHeaders<K, V, DefaultHeadersImpl<K, V>> {
    public DefaultHeadersImpl(HashingStrategy<K> nameHashingStrategy,
            ValueConverter<V> valueConverter, NameValidator<K> nameValidator) {
        super(nameHashingStrategy, valueConverter, nameValidator);
    }

    public DefaultHeadersImpl(HashingStrategy<K> nameHashingStrategy, ValueConverter<V> valueConverter,
                              NameValidator<K> nameValidator, int arraySizeHint, ValueValidator<V> valueValidator) {
        super(nameHashingStrategy, valueConverter, nameValidator, arraySizeHint, valueValidator);
    }
}
