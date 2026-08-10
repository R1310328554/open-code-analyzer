/*
 * Copyright 2016 The Netty Project
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

/** HTTP/2 RST_STREAM 帧：单方面终止流，不经过正常 END_STREAM 握手。 */
public interface Http2ResetFrame extends Http2StreamFrame {

    /**
     * 重置原因，以 HTTP/2 错误码表示（如 {@code CANCEL}、{@code PROTOCOL_ERROR}）。
     */
    long errorCode();
}
