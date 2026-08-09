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
package org.redisson.api.fanout;

import org.redisson.api.MessageArgs;
import org.redisson.api.SyncArgs;
import org.redisson.client.codec.Codec;

/**
 * 可靠扇出（Reliable Fanout）发布操作的参数构建接口。
 * <p>
 * 通过 {@link #messages(MessageArgs[])} 静态工厂创建实例，可链式设置消息头编解码器。
 *
 * @param <V> 消息体类型
 * @author Nikita Koksharov
 */
public interface FanoutPublishArgs<V> extends SyncArgs<FanoutPublishArgs<V>> {

    /**
     * 设置消息头编解码所用的 {@link org.redisson.client.codec.Codec}。
     *
     * @param codec 编解码器
     * @return 当前参数对象，支持链式调用
     */
    FanoutPublishArgs<V> headersCodec(Codec codec);

    /**
     * 定义待发布的一条或多条消息。
     *
     * @param msgs 要加入扇出队列的消息参数
     * @return 参数构建对象
     */
    @SafeVarargs
    static <V> FanoutPublishArgs<V> messages(MessageArgs<V>... msgs) {
        return new FanoutPublishParams<>(msgs);
    }

}
