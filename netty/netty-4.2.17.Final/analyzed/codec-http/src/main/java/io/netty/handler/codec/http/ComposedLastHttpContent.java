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
package io.netty.handler.codec.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderResult;


/**
 * 仅含尾部头（trailing headers）、无正文内容的 {@link LastHttpContent} 实现。
 * <p>
 * 用于 chunked 编码结束但需传递 trailing headers 的场景；{@link #content()} 恒为空缓冲。
 */
final class ComposedLastHttpContent implements LastHttpContent {
    private final HttpHeaders trailingHeaders;
    private DecoderResult result;

    /** 创建仅含 trailing headers 的实例。 */
    ComposedLastHttpContent(HttpHeaders trailingHeaders) {
        this.trailingHeaders = trailingHeaders;
    }

    /** 创建含 trailing headers 与解码结果的实例。 */
    ComposedLastHttpContent(HttpHeaders trailingHeaders, DecoderResult result) {
        this(trailingHeaders);
        this.result = result;
    }

    /** 返回 chunked 尾部头。 */
    @Override
    public HttpHeaders trailingHeaders() {
        return trailingHeaders;
    }

    @Override
    public LastHttpContent copy() {
        LastHttpContent content = new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
        content.trailingHeaders().set(trailingHeaders());
        return content;
    }

    @Override
    public LastHttpContent duplicate() {
        return copy();
    }

    @Override
    public LastHttpContent retainedDuplicate() {
        return copy();
    }

    @Override
    public LastHttpContent replace(ByteBuf content) {
        final LastHttpContent dup = new DefaultLastHttpContent(content);
        dup.trailingHeaders().setAll(trailingHeaders());
        return dup;
    }

    @Override
    public LastHttpContent retain(int increment) {
        return this;
    }

    @Override
    public LastHttpContent retain() {
        return this;
    }

    @Override
    public LastHttpContent touch() {
        return this;
    }

    @Override
    public LastHttpContent touch(Object hint) {
        return this;
    }

    @Override
    public ByteBuf content() {
        return Unpooled.EMPTY_BUFFER;
    }

    @Override
    public DecoderResult decoderResult() {
        return result;
    }

    @Override
    public DecoderResult getDecoderResult() {
        return decoderResult();
    }

    @Override
    public void setDecoderResult(DecoderResult result) {
        this.result = result;
    }

    @Override
    public int refCnt() {
        return 1;
    }

    @Override
    public boolean release() {
        return false;
    }

    @Override
    public boolean release(int decrement) {
        return false;
    }
}
