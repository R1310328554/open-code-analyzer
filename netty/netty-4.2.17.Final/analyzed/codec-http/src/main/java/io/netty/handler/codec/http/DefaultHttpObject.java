/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.http;

import io.netty.handler.codec.DecoderResult;
import io.netty.util.internal.ObjectUtil;

/**
 * 默认 {@link HttpObject} 基类，持有解码结果 {@link DecoderResult}。
 */
public class DefaultHttpObject implements HttpObject {

    private static final int HASH_CODE_PRIME = 31;
    /** HTTP 解码器对该对象的解析结果，默认成功。 */
    private DecoderResult decoderResult = DecoderResult.SUCCESS;

    protected DefaultHttpObject() {
        // 禁止直接实例化，仅供子类继承
    }

    /** 返回解码结果（成功或失败详情）。 */
    @Override
    public DecoderResult decoderResult() {
        return decoderResult;
    }

    @Override
    @Deprecated
    public DecoderResult getDecoderResult() {
        return decoderResult();
    }

    /** 由解码器设置解析结果。 */
    @Override
    public void setDecoderResult(DecoderResult decoderResult) {
        this.decoderResult = ObjectUtil.checkNotNull(decoderResult, "decoderResult");
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = HASH_CODE_PRIME * result + decoderResult.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultHttpObject)) {
            return false;
        }

        DefaultHttpObject other = (DefaultHttpObject) o;

        return decoderResult().equals(other.decoderResult());
    }
}
