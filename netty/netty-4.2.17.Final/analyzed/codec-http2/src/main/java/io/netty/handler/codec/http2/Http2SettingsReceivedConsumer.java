/*
 * Copyright 2019 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

/**
 * 消费端接口：处理已收到但尚未发送 ACK 的对端 SETTINGS。
 * <p>典型用于在 ACK 发出前预览或暂存对端参数，避免设置立即生效。
 */
public interface Http2SettingsReceivedConsumer {
    /**
     * 消费最近收到、尚未 ACK 的设置项。
     */
    void consumeReceivedSettings(Http2Settings settings);
}
