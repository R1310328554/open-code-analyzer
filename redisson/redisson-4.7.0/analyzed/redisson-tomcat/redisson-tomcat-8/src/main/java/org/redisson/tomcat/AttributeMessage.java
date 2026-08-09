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
package org.redisson.tomcat;

import java.io.IOException;
import java.io.Serializable;

import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

/**
 * 集群 HTTP Session 属性同步消息的基类（Apache Tomcat）。
 * <p>携带发起节点 ID 与目标 Session ID，并提供 Redisson 编解码辅助方法。
 *
 * @author Nikita Koksharov
 */
public class AttributeMessage implements Serializable {

    private String sessionId;

    private String nodeId;

    public AttributeMessage() {
    }
    
    /** @param nodeId 发起变更的 Tomcat 节点标识
     *  @param sessionId 目标 HTTP Session ID
     */
    public AttributeMessage(String nodeId, String sessionId) {
        this.nodeId = nodeId;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getNodeId() {
        return nodeId;
    }
    
    /** 使用 Redisson {@link Encoder} 将属性值序列化为字节数组。 */
	protected byte[] toByteArray(Encoder encoder, Object value) throws IOException {
		if (value == null) {
			return null;
		}
		
		ByteBuf buf = encoder.encode(value);
		try {
		    return ByteBufUtil.getBytes(buf);
        } finally {
            buf.release();
        }
	}
	
    /** 使用 Redisson {@link Decoder} 从字节数组反序列化属性值。 */
	protected Object toObject(Decoder<?> decoder, byte[] value) throws IOException, ClassNotFoundException {
    	if (value == null) {
    		return null;
    	}
    	
    	ByteBuf buf = Unpooled.wrappedBuffer(value);
    	try {
    	    return decoder.decode(buf, null);
        } finally {
            buf.release();
        }
	}
}
