/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec.socksx.v4;

/**
 * A SOCKS4a {@code CONNECT} or {@code BIND} request.
 *
 * <p>SOCKS4/4a 命令请求的消息接口。线格式：VN(4) + CD + DSTPORT + DSTIP + USERID + NUL
 * （4a 域名模式下 DSTIP 为 0.0.0.x 占位，USERID 后追加域名 + NUL）。</p>
 */
public interface Socks4CommandRequest extends Socks4Message {

    /**
     * Returns the type of this request.
     *
     * <p>命令类型：{@link Socks4CommandType#CONNECT} 或 {@link Socks4CommandType#BIND}。</p>
     */
    Socks4CommandType type();

    /**
     * Returns the {@code USERID} field of this request.
     *
     * <p>可选的用户标识字符串，以 NUL 终止；空串表示未提供 ident 信息。</p>
     */
    String userId();

    /**
     * Returns the {@code DSTIP} field of this request.
     *
     * <p>目标地址：IPv4 点分字符串，或 SOCKS4a 模式下的域名。</p>
     */
    String dstAddr();

    /**
     * Returns the {@code DSTPORT} field of this request.
     *
     * <p>目标端口，网络字节序字段的无符号 16 位值（1~65535）。</p>
     */
    int dstPort();
}
