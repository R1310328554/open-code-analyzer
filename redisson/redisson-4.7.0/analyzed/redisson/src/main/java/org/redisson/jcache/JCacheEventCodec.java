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
package org.redisson.jcache;

import io.netty.buffer.ByteBuf;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.codec.BaseEventCodec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JCache Pub/Sub 事件的 Netty 解码器。
 * <p>
 * 从频道消息解析 key、value、可选 oldValue 与 syncId；
 * 继承 {@link BaseEventCodec} 复用 map key/value 解码逻辑。
 *
 * @author Nikita Koksharov
 *
 */
public class JCacheEventCodec extends BaseEventCodec {

    /** 消息尾部是否包含集群同步用的 syncId（double）。 */
    private final boolean sync;
    /** 是否期望消息中带 oldValue 字段（UPDATE 监听器计数 &gt; 0 时）。 */
    private final boolean expectOldValueInMsg;
    
    /** 将一条 Pub/Sub 二进制消息解码为 List：key, value, [oldValue], [syncId]。 */
    private final Decoder<Object> decoder = new Decoder<Object>() {
        @Override
        /** 按序解码 key、value，按需解码 oldValue 与 syncId。 */
        public Object decode(ByteBuf buf, State state) throws IOException {
            List<Object> result = new ArrayList<>();

            Object key = JCacheEventCodec.this.decode(buf, state, codec.getMapKeyDecoder());
            result.add(key);

            Object value = JCacheEventCodec.this.decode(buf, state, codec.getMapValueDecoder());
            result.add(value);

            // 旧值占位：-1 表示无 oldValue
            if (expectOldValueInMsg) {
                if (buf.getShortLE(buf.readerIndex()) != -1) {
                    Object oldValue = JCacheEventCodec.this.decode(buf, state, codec.getMapValueDecoder());
                    result.add(oldValue);
                } else {
                    buf.readShortLE();
                    result.add(null);
                }
            }
            
            // 同步模式追加 syncId 供 waitSync 使用
            if (sync) {
                double syncId = buf.readDoubleLE();
                result.add(syncId);
            }
            
            return result;
        }
    };

    /** 构造事件编解码器（不含 oldValue 字段）。 */
    public JCacheEventCodec(Codec codec, OSType osType, boolean sync) {
        super(codec, osType);
        this.sync = sync;
        this.expectOldValueInMsg = false;
    }

    /** 构造事件编解码器，可指定是否解析 oldValue。 */
    public JCacheEventCodec(Codec codec, OSType osType, boolean sync, boolean expectOldValueInMsg) {
        super(codec, osType);
        this.sync = sync;
        this.expectOldValueInMsg = expectOldValueInMsg;
    }

    /** 返回 Pub/Sub 消息体的自定义解码器。 */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

}
