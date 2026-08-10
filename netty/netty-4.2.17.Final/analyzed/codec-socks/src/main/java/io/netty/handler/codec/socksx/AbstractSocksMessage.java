/*
 * Copyright 2014 The Netty Project
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

package io.netty.handler.codec.socksx;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;

/**
 * An abstract {@link SocksMessage}.
 *
 * <p>{@code socksx} 包中所有 SOCKS 消息的公共基类，实现 {@link DecoderResultProvider} 契约。
 * 解码器可将部分解析失败的结果写入 {@link #decoderResult}，供业务层在消息仍可读字段时做降级处理。</p>
 */
public abstract class AbstractSocksMessage implements SocksMessage {

    /** 默认表示解码完全成功；失败时由解码器调用 {@link #setDecoderResult} 覆盖。 */
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        this.decoderResult = ObjectUtil.checkNotNull(decoderResult, "decoderResult");
    }
}
