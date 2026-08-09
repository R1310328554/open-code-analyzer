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
package org.redisson.client.protocol.decoder;

/**
 * 简单的键值对消息载体，用于 Pub/Sub 或协议层传递单条 KV 数据。
 *
 * @author Nikita Koksharov
 *
 * @param <K> key type
 * @param <V> value type
 */
public class KeyValueMessage<K, V> {

    /** 消息键。 */
    private K key;
    /** 消息值。 */
    private V value;

    /** 构造不可变语义上的键值对（字段本身可变）。 */
    public KeyValueMessage(K key, V value) {
        super();
        this.key = key;
        this.value = value;
    }

    /** 返回键。 */
    public K getKey() {
        return key;
    }

    /** 返回值。 */
    public V getValue() {
        return value;
    }

}
