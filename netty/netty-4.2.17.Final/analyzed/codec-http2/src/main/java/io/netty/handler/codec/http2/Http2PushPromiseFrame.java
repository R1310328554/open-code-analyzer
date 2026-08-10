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
 * HTTP/2 {@code PUSH_PROMISE} 帧对象：服务端主动推送资源，预先声明将开启的关联流及其请求头。
 */
public interface Http2PushPromiseFrame extends Http2StreamFrame {

    /**
     * 绑定被推送（promised）流的 {@link Http2FrameStream} 对象。
     */
    Http2StreamFrame pushStream(Http2FrameStream stream);

    /**
     * 返回 promised 流对象；尚未关联流时返回 {@code null}。
     */
    Http2FrameStream pushStream();

    /**
     * PUSH_PROMISE 携带的请求头（伪头部 + 常规头部）。
     */
    Http2Headers http2Headers();

    /**
     * 帧填充字节数，非负且小于 256。
     */
    int padding();

    /**
     * 服务端预分配的 promised stream id。
     */
    int promisedStreamId();

    @Override
    Http2PushPromiseFrame stream(Http2FrameStream stream);

}
