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
 * Type of socks request
 *
 * <p>标记 {@link SocksRequest} 在 SOCKS5 客户端状态机中的阶段。
 * 正常流程为 INIT →（可选 AUTH）→ CMD；解码失败时落入 {@link #UNKNOWN}。</p>
 */
public enum SocksRequestType {
    /** 方法协商请求（{@link SocksInitRequest}，RFC 1928 握手第一步）。 */
    INIT,
    /** 用户名/密码子协商请求（{@link SocksAuthRequest}，RFC 1929）。 */
    AUTH,
    /** 连接/绑定等命令请求（{@link SocksCmdRequest}，RFC 1928 命令阶段）。 */
    CMD,
    /** 无法识别或解析失败的请求占位类型（{@link UnknownSocksRequest}）。 */
    UNKNOWN
}
