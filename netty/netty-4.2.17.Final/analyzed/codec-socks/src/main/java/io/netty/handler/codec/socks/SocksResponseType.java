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
 * Type of socks response
 *
 * <p>标记 {@link SocksResponse} 在 SOCKS5 服务端状态机中的阶段，
 * 与 {@link SocksRequestType} 一一对应，便于编解码器按阶段切换 pipeline handler。</p>
 */
public enum SocksResponseType {
    /** 方法协商响应（{@link SocksInitResponse}）。 */
    INIT,
    /** 用户名/密码子协商响应（{@link SocksAuthResponse}）。 */
    AUTH,
    /** 命令执行结果响应（{@link SocksCmdResponse}）。 */
    CMD,
    /** 无法识别或解析失败的响应占位类型（{@link UnknownSocksResponse}）。 */
    UNKNOWN
}
