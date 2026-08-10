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
package io.netty.handler.codec.http3;

/**
 * Marker interface for frames that can be sent and received on a
 * <a href="https://tools.ietf.org/html/draft-ietf-quic-http-32#section-7">HTTP3 push stream</a>.
 * <p>Push 流为服务端发起的单向 QUIC 流，典型帧序为 HEADERS（响应头）+ DATA（实体）；
 * 不含 PUSH_PROMISE、SETTINGS 等控制流帧类型。
 */
public interface Http3PushStreamFrame extends Http3Frame {
}
