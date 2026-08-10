/*
 * Copyright 2021 The Netty Project
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
package io.netty.handler.codec.mqtt;

/** MQTT 编解码器的默认阈值常量，供 {@link MqttDecoder}/{@link MqttEncoder} 构造时使用。 */
public final class MqttConstant {

    private MqttConstant() {
    }

    /**
     * Default max bytes in message
     * <p>单条 MQTT 报文允许的最大字节数，超出则 {@link MqttDecoder} 抛出 {@link io.netty.handler.codec.TooLongFrameException}。</p>
     */
    public static final int DEFAULT_MAX_BYTES_IN_MESSAGE = 8092;

    /**
     * min client id length
     * <p>MQTT 3.1 规范要求 clientId 至少 1 字节（3.1.1/5.0 允许零长度）。</p>
     */
    public static final int MIN_CLIENT_ID_LENGTH = 1;

    /**
     * Default max client id length,In the mqtt3.1 protocol,
     * the default maximum Client Identifier length is 23
     * <p>MQTT 3.1 协议下 clientId 默认上限 23 字节；3.1.1/5.0 由服务端自行决定。</p>
     */
    public static final int DEFAULT_MAX_CLIENT_ID_LENGTH = 23;

}
