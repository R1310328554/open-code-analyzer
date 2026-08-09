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
package org.redisson.api.queue;

import org.redisson.api.MessageArgs;
import org.redisson.api.SyncArgs;
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * 定义队列消息添加操作的参数。
 *
 * @param <V> 消息值类型
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueAddArgs<V> extends SyncArgs<QueueAddArgs<V>> {

    /**
     * 设置向已满且容量受限的队列添加消息时的最长等待时间。
     *
     * @param value 最长等待时间
     * @return 参数对象
     */
    QueueAddArgs<V> timeout(Duration value);

    /**
     * 设置用于编解码消息头字段值的 Codec。
     *
     * @param codec 编解码器
     * @return 参数对象
     */
    QueueAddArgs<V> headersCodec(Codec codec);

    /**
     * 指定待添加的消息。
     *
     * @param msgs 要加入队列的消息参数
     * @return 参数对象
     */
    @SafeVarargs
    static <V> QueueAddArgs<V> messages(MessageArgs<V>... msgs) {
        return new QueueAddParams<>(msgs);
    }

}
