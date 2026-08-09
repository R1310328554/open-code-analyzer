/*
 * Copyright 2020 The Netty Project
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
package io.netty.handler.codec.http2;

/**
 * {@link Http2PushPromiseFrame} 的默认实现，承载服务器推送（Server Push）承诺帧。
 * <p>服务器通过该帧告知客户端即将推送的资源头信息；{@code promisedStreamId} 标识
 * 将被创建的新流，{@code streamFrame} 则关联发起推送的父流。
 */
public final class DefaultHttp2PushPromiseFrame implements Http2PushPromiseFrame {

    /** 被承诺（即将推送）的流对象，优先于构造时缓存的 promisedStreamId。 */
    private Http2FrameStream pushStreamFrame;
    /** 推送资源的 HTTP/2 头（:method、:path 等伪头与普通头）。 */
    private final Http2Headers http2Headers;
    /** 发送 PUSH_PROMISE 的父流（客户端原始请求所在流）。 */
    private Http2FrameStream streamFrame;
    /** 帧尾填充字节数，用于流量分析混淆或对齐。 */
    private final int padding;
    /** 承诺流 ID 的占位值；{@code pushStreamFrame} 未绑定时才直接返回。 */
    private final int promisedStreamId;

    public DefaultHttp2PushPromiseFrame(Http2Headers http2Headers) {
        this(http2Headers, 0);
    }

    public DefaultHttp2PushPromiseFrame(Http2Headers http2Headers, int padding) {
        this(http2Headers, padding, -1);
    }

    DefaultHttp2PushPromiseFrame(Http2Headers http2Headers, int padding, int promisedStreamId) {
        this.http2Headers = http2Headers;
        this.padding = padding;
        this.promisedStreamId = promisedStreamId;
    }

    @Override
    public Http2StreamFrame pushStream(Http2FrameStream stream) {
        pushStreamFrame = stream;
        return this;
    }

    @Override
    public Http2FrameStream pushStream() {
        return pushStreamFrame;
    }

    @Override
    public Http2Headers http2Headers() {
        return http2Headers;
    }

    @Override
    public int padding() {
        return padding;
    }

    @Override
    public int promisedStreamId() {
        if (pushStreamFrame != null) {
            return pushStreamFrame.id();
        } else {
            return promisedStreamId;
        }
    }

    @Override
    public Http2PushPromiseFrame stream(Http2FrameStream stream) {
        streamFrame = stream;
        return this;
    }

    @Override
    public Http2FrameStream stream() {
        return streamFrame;
    }

    @Override
    public String name() {
        return "PUSH_PROMISE_FRAME";
    }

    @Override
    public String toString() {
        return "DefaultHttp2PushPromiseFrame{" +
                "pushStreamFrame=" + pushStreamFrame +
                ", http2Headers=" + http2Headers +
                ", streamFrame=" + streamFrame +
                ", padding=" + padding +
                '}';
    }
}
