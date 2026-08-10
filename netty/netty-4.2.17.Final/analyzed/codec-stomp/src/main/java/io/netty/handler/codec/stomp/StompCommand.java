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
package io.netty.handler.codec.stomp;

/**
 * STOMP command
 * <p>STOMP 协议帧首行的命令字枚举，对应 {@link StompHeadersSubframe#command()}。
 * {@link StompSubframeDecoder} 解析首行后映射为此类型；无法识别时回退为 {@link #UNKNOWN}。</p>
 */
public enum StompCommand {
    /** STOMP 1.2 协议握手命令。 */
    STOMP,
    /** 客户端发起连接。 */
    CONNECT,
    /** 服务端连接成功响应。 */
    CONNECTED,
    /** 向目的地发送消息。 */
    SEND,
    /** 订阅目的地。 */
    SUBSCRIBE,
    /** 取消订阅。 */
    UNSUBSCRIBE,
    /** 确认已消费消息。 */
    ACK,
    /** 否定确认（消息未成功处理）。 */
    NACK,
    /** 开启事务。 */
    BEGIN,
    /** 中止事务。 */
    ABORT,
    /** 提交事务。 */
    COMMIT,
    /** 断开连接。 */
    DISCONNECT,
    /** 服务端推送的消息帧。 */
    MESSAGE,
    /** 服务端对需回执请求的确认。 */
    RECEIPT,
    /** 服务端错误响应。 */
    ERROR,
    /** 无法解析或未知的命令字。 */
    UNKNOWN
}
