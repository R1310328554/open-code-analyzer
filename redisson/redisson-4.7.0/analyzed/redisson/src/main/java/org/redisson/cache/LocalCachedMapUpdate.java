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
package org.redisson.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.netty.buffer.ByteBuf;

/**
 * 本地缓存条目更新广播消息。
 * <p>
 * 向其他实例广播完整键值字节，触发本地缓存条目写入或更新。
 *
 * @author Nikita Koksharov
 *
 */
@SuppressWarnings("serial")
public class LocalCachedMapUpdate implements Serializable {

    /** 单条键值更新条目。 */
    public static class Entry {
        
        /** 编码后的键字节数组。 */
        private final byte[] key;
        /** 编码后的值字节数组。 */
        private final byte[] value;
        
        public Entry(byte[] key, byte[] value) {
            this.key = key;
            this.value = value;
        }
        
        public Entry(ByteBuf keyBuf, ByteBuf valueBuf) {
            key = new byte[keyBuf.readableBytes()];
            keyBuf.getBytes(keyBuf.readerIndex(), key);
            
            value = new byte[valueBuf.readableBytes()];
            valueBuf.getBytes(valueBuf.readerIndex(), value);
        }

        public byte[] getKey() {
            return key;
        }
        
        public byte[] getValue() {
            return value;
        }
        
    }
    
    /** 待更新的键值条目列表。 */
    private List<Entry> entries = new ArrayList<Entry>();

    /** 发起更新操作的实例 ID（接收方需排除自身）。 */
    private byte[] excludedId;

    public LocalCachedMapUpdate() {
    }
    
    public LocalCachedMapUpdate(byte[] excludedId, List<Entry> entries) {
        super();
        this.excludedId = excludedId;
        this.entries = entries;
    }

    public LocalCachedMapUpdate(byte[] excludedId, ByteBuf keyBuf, ByteBuf valueBuf) {
        this.excludedId = excludedId;
        byte[] key = new byte[keyBuf.readableBytes()];
        keyBuf.getBytes(keyBuf.readerIndex(), key);
        
        byte[] value = new byte[valueBuf.readableBytes()];
        valueBuf.getBytes(valueBuf.readerIndex(), value);
        entries = Collections.singletonList(new Entry(key, value));
    }
    
    public LocalCachedMapUpdate(byte[] key, byte[] value) {
        entries = Collections.singletonList(new Entry(key, value));
    }

    public Collection<Entry> getEntries() {
        return entries;
    }

    public byte[] getExcludedId() {
        return excludedId;
    }
}
