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

package io.netty.handler.codec.socks;

/**
 * {@link SocksMessage} 在协议方向上的粗粒度分类。
 * <p>用于 {@link SocksMessage#type()} 区分客户端 {@link SocksRequest} 与服务器
 * {@link SocksResponse}；解码失败时可能得到 {@link #UNKNOWN} 占位消息。</p>
 */
public enum SocksMessageType {
    /** 客户端发往代理的请求消息。 */
    REQUEST,
    /** 代理返回的应答消息。 */
    RESPONSE,
    /** 无法解析或协议版本不匹配时的占位类型。 */
    UNKNOWN
}
