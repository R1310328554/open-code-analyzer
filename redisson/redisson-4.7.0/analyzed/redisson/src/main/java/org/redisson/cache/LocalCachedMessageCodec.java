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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地缓存同步消息的编解码器。
 * <p>
 * 支持清空、失效、更新、禁用/启用及确认等多种消息类型的序列化。
 *
 * @author Nikita Koksharov
 *
 */
public class LocalCachedMessageCodec extends BaseCodec {

    /** 单例实例。 */
    public static final LocalCachedMessageCodec INSTANCE = new LocalCachedMessageCodec();
    
    private final Decoder<Object> decoder = (buf, state) -> {
        byte type = buf.readByte();
        if (type == 0x0) { // 清空消息
            byte[] excludedId = new byte[16];
            buf.readBytes(excludedId);
            byte[] id = new byte[16];
            buf.readBytes(id);
            boolean releaseSemaphore = buf.readBoolean();
            return new LocalCachedMapClear(excludedId, id, releaseSemaphore);
        }

        if (type == 0x1) { // 失效消息
            byte[] excludedId = new byte[16];
            buf.readBytes(excludedId);
            int hashesCount = buf.readInt();
            byte[][] hashes = new byte[hashesCount][];
            for (int i = 0; i < hashesCount; i++) {
                byte[] keyHash = new byte[16];
                buf.readBytes(keyHash);
                hashes[i] = keyHash;
            }
            return new LocalCachedMapInvalidate(excludedId, hashes);
        }

        if (type == 0x2) { // 更新消息
            byte[] excludedId = new byte[16];
            buf.readBytes(excludedId);
            List<LocalCachedMapUpdate.Entry> entries = new ArrayList<LocalCachedMapUpdate.Entry>();
            while (true) {
                int keyLen = buf.readInt();
                byte[] key = new byte[keyLen];
                buf.readBytes(key);
                int valueLen = buf.readInt();
                byte[] value = new byte[valueLen];
                buf.readBytes(value);
                entries.add(new LocalCachedMapUpdate.Entry(key, value));

                if (!buf.isReadable()) {
                    break;
                }
            }
            return new LocalCachedMapUpdate(excludedId, entries);
        }

        if (type == 0x3) { // 禁用消息
            byte len = buf.readByte();
            CharSequence requestId = buf.readCharSequence(len, CharsetUtil.US_ASCII);
            long timeout = buf.readLong();
            boolean disableCache = buf.readBoolean();
            int hashesCount = buf.readInt();
            byte[][] hashes = new byte[hashesCount][];
            for (int i = 0; i < hashesCount; i++) {
                byte[] keyHash = new byte[16];
                buf.readBytes(keyHash);
                hashes[i] = keyHash;
            }
            return new LocalCachedMapDisable(requestId.toString(), hashes, timeout, disableCache);
        }

        if (type == 0x4) { // 禁用确认
            return new LocalCachedMapDisableAck();
        }

        if (type == 0x5) { // 启用消息
            byte len = buf.readByte();
            CharSequence requestId = buf.readCharSequence(len, CharsetUtil.UTF_8);
            boolean enableCache = buf.readBoolean();
            int hashesCount = buf.readInt();
            byte[][] hashes = new byte[hashesCount][];
            for (int i = 0; i < hashesCount; i++) {
                byte[] keyHash = new byte[16];
                buf.readBytes(keyHash);
                hashes[i] = keyHash;
            }
            return new LocalCachedMapEnable(requestId.toString(), hashes, enableCache);
        }

        if (type == 0x6) { // 禁用键记录
            byte len = buf.readByte();
            CharSequence requestId = buf.readCharSequence(len, CharsetUtil.US_ASCII);
            long timeout = buf.readLong();
            return new LocalCachedMapDisabledKey(requestId.toString(), timeout);
        }

        throw new IllegalArgumentException("无法解析本地缓存消息包");
    };
    
    private final Encoder encoder = in -> {
        if (in instanceof LocalCachedMapClear) {
            LocalCachedMapClear li = (LocalCachedMapClear) in;
            ByteBuf result = ByteBufAllocator.DEFAULT.buffer(1);
            result.writeByte(0x0);
            result.writeBytes(li.getExcludedId());
            result.writeBytes(li.getRequestId());
            result.writeBoolean(li.isReleaseSemaphore());
            return result;
        }
        if (in instanceof LocalCachedMapInvalidate) {
            LocalCachedMapInvalidate li = (LocalCachedMapInvalidate) in;
            ByteBuf result = ByteBufAllocator.DEFAULT.buffer();
            result.writeByte(0x1);

            result.writeBytes(li.getExcludedId());
            result.writeInt(li.getKeyHashes().length);
            for (int i = 0; i < li.getKeyHashes().length; i++) {
                result.writeBytes(li.getKeyHashes()[i]);
            }
            return result;
        }

        if (in instanceof LocalCachedMapUpdate) {
            LocalCachedMapUpdate li = (LocalCachedMapUpdate) in;
            ByteBuf result = ByteBufAllocator.DEFAULT.buffer();
            result.writeByte(0x2);

            result.writeBytes(li.getExcludedId());
            for (LocalCachedMapUpdate.Entry e : li.getEntries()) {
                result.writeInt(e.getKey().length);
                result.writeBytes(e.getKey());
                result.writeInt(e.getValue().length);
                result.writeBytes(e.getValue());
            }
            return result;
        }

        if (in instanceof LocalCachedMapDisable) {
            LocalCachedMapDisable li = (LocalCachedMapDisable) in;
            ByteBuf result = ByteBufAllocator.DEFAULT.buffer();
            result.writeByte(0x3);

            result.writeByte(li.getRequestId().length());
            result.writeCharSequence(li.getRequestId(), CharsetUtil.UTF_8);
            result.writeLong(li.getTimeout());
            result.writeBoolean(li.isDisableCache());
            result.writeInt(li.getKeyHashes().length);
            for (int i = 0; i < li.getKeyHashes().length; i++) {
                result.writeBytes(li.getKeyHashes()[i]);
            }
            return result;
        }

        if (in instanceof LocalCachedMapDisableAck) {
            ByteBuf result = ByteBufAllocator.DEFAULT.buffer(1);
            result.writeByte(0x4);
            return result;
        }

        if (in instanceof LocalCachedMapEnable) {
            LocalCachedMapEnable li = (LocalCachedMapEnable) in;
            ByteBuf result = ByteBufAllocator.DEFAULT.buffer();
            result.writeByte(0x5);

            result.writeByte(li.getRequestId().length());
            result.writeCharSequence(li.getRequestId(), CharsetUtil.UTF_8);
            result.writeBoolean(li.isEnableCache());
            result.writeInt(li.getKeyHashes().length);
            for (int i = 0; i < li.getKeyHashes().length; i++) {
                result.writeBytes(li.getKeyHashes()[i]);
            }
            return result;
        }

        if (in instanceof LocalCachedMapDisabledKey) {
            LocalCachedMapDisabledKey dk = (LocalCachedMapDisabledKey) in;
            ByteBuf result = ByteBufAllocator.DEFAULT.buffer();
            result.writeByte(0x6);

            result.writeByte(dk.getRequestId().length());
            result.writeCharSequence(dk.getRequestId(), CharsetUtil.UTF_8);
            result.writeLong(dk.getTimeout());
            return result;
        }

        throw new IllegalArgumentException("无法编码本地缓存消息包 " + in);
    };


    public LocalCachedMessageCodec() {
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

}
