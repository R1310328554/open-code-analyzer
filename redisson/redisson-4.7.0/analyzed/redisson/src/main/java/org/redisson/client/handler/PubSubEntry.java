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
package org.redisson.client.handler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.redisson.client.protocol.decoder.MultiDecoder;
import org.redisson.client.protocol.pubsub.Message;

/**
 * 单个 Pub/Sub 频道的消息缓冲条目，用于保序投递。
 * <p>
 * 包含消息队列、解码器及发送中标志。
 *
 * @author Nikita Koksharov
 *
 */
public class PubSubEntry {

    /** 该频道消息体使用的复合解码器。 */
    private final MultiDecoder<Object> decoder;
    
    /** 待投递的 Pub/Sub 消息队列。 */
    private final Queue<Message> queue = new ConcurrentLinkedQueue<Message>();

    /** 是否已有线程正在向连接投递消息。 */
    private final AtomicBoolean sent = new AtomicBoolean();
    
    /** @param decoder 频道消息解码器 */
    public PubSubEntry(MultiDecoder<Object> decoder) {
        super();
        this.decoder = decoder;
    }
    
    /** 返回消息解码器。 */
    public MultiDecoder<Object> getDecoder() {
        return decoder;
    }
    
    /** 返回待投递消息队列。 */
    public Queue<Message> getQueue() {
        return queue;
    }
    
    /** 返回发送中标志。 */
    public AtomicBoolean getSent() {
        return sent;
    }
    
}
