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
package org.redisson.api.pubsub;

import org.redisson.api.MessageArgs;
import org.redisson.api.SyncArgs;
import org.redisson.client.codec.Codec;

import java.time.Duration;

/**
 * 定义主题消息发布操作的参数接口。
 *
 * @param <V> 消息值类型
 *
 * @author Nikita Koksharov
 *
 */
public interface PublishArgs<V> extends SyncArgs<PublishArgs<V>> {

    /**
     * 设置向容量有限且已满的主题追加消息时的最长等待时间。
     *
     * @param value 最长等待时间
     * @return 参数对象
     */
    PublishArgs<V> timeout(Duration value);

    /**
     * 设置用于编解码消息头字段值的 {@link Codec}。
     *
     * @param codec 编解码器
     * @return 参数对象
     */
    PublishArgs<V> headersCodec(Codec codec);

    /**
     * 指定要追加到主题的消息。
     *
     * @param msgs 要添加到主题的消息参数
     * @return 参数对象
     */
    @SafeVarargs
    static <V> PublishArgs<V> messages(MessageArgs<V>... msgs) {
        return new PublishParams<>(msgs);
    }

}
