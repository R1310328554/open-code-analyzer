/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.memcache.binary;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.UnstableApi;

/**
 * The default implementation of the {@link BinaryMemcacheRequest}.
 *
 * <p>{@link BinaryMemcacheRequest} 的默认实现：构造时自动设置 magic 为 {@link #REQUEST_MAGIC_BYTE}（0x80），
 * key/extras 的引用计数由父类 {@link AbstractBinaryMemcacheMessage} 统一管理。</p>
 */
@UnstableApi
public class DefaultBinaryMemcacheRequest extends AbstractBinaryMemcacheMessage implements BinaryMemcacheRequest {

    /**
     * Default magic byte for a request.
     */
    public static final byte REQUEST_MAGIC_BYTE = (byte) 0x80;

    /** 24 字节头中第 7–8 字节，请求侧保留字段（响应侧为 status）。 */
    private short reserved;

    /**
     * Create a new {@link DefaultBinaryMemcacheRequest} with the header only.
     */
    public DefaultBinaryMemcacheRequest() {
        this(null, null);
    }

    /**
     * Create a new {@link DefaultBinaryMemcacheRequest} with the header and key.
     *
     * @param key    the key to use.
     */
    public DefaultBinaryMemcacheRequest(ByteBuf key) {
        this(key, null);
    }

    /**
     * Create a new {@link DefaultBinaryMemcacheRequest} with the header only.
     *
     * @param key    the key to use.
     * @param extras the extras to use.
     */
    public DefaultBinaryMemcacheRequest(ByteBuf key, ByteBuf extras) {
        super(key, extras);
        setMagic(REQUEST_MAGIC_BYTE);
    }

    @Override
    public short reserved() {
        return reserved;
    }

    @Override
    public BinaryMemcacheRequest setReserved(short reserved) {
        this.reserved = reserved;
        return this;
    }

    @Override
    public BinaryMemcacheRequest retain() {
        super.retain();
        return this;
    }

    @Override
    public BinaryMemcacheRequest retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public BinaryMemcacheRequest touch() {
        super.touch();
        return this;
    }

    @Override
    public BinaryMemcacheRequest touch(Object hint) {
        super.touch(hint);
        return this;
    }

    /**
     * Copies special metadata hold by this instance to the provided instance
     *
     * @param dst The instance where to copy the metadata of this instance to
     */
    void copyMeta(DefaultBinaryMemcacheRequest dst) {
        super.copyMeta(dst);
        dst.reserved = reserved;
    }
}
