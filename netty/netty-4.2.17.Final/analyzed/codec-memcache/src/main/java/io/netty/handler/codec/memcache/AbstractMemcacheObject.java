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
package io.netty.handler.codec.memcache;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.UnstableApi;

/**
 * The default {@link MemcacheObject} implementation.
 *
 * <p>Memcache 消息对象的公共基类：继承 {@link AbstractReferenceCounted} 管理 {@link ByteBuf} 生命周期，
 * 并持有 {@link DecoderResult}，使解码失败时 upstream 仍能收到带失败标记的对象而非直接抛异常。
 */
@UnstableApi
public abstract class AbstractMemcacheObject extends AbstractReferenceCounted implements MemcacheObject {

    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    protected AbstractMemcacheObject() {
        // 禁止直接实例化，仅允许子类构造
    }

    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        this.decoderResult = ObjectUtil.checkNotNull(result, "DecoderResult should not be null.");
    }
}
