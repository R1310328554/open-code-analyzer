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
package org.redisson.micronaut.session;

import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;

/**
 * 跨节点广播：更新 Session 中单个属性的值。
 * <p>构造时将值编码为 {@code byte[]} 以便 Redis 发布/订阅传输。
 *
 * @author Nikita Koksharov
 */
public class AttributeUpdateMessage extends AttributeMessage {

    private String name;
    private byte[] value;

    public AttributeUpdateMessage() {
    }
    
    /** @param name 属性名
     *  @param value 新属性值
     *  @param encoder Redisson 编码器
     */
    public AttributeUpdateMessage(String nodeId, String sessionId, String name, Object value, Encoder encoder) throws IOException {
        super(nodeId, sessionId);
        this.name = name;
		this.value = toByteArray(encoder, value);
    }

    public String getName() {
        return name;
    }
    
    /** 使用给定解码器还原属性值。 */
    public Object getValue(Decoder<?> decoder) throws IOException, ClassNotFoundException {
    	return toObject(decoder, value);
    }
    
}
