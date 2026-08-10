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
 * A SOCKS4a response.
 *
 * <p>SOCKS4 代理对命令请求的应答。固定 8 字节：VN(0) + CD(状态) + DSTPORT + DSTIP。
 * BIND 成功时 DSTIP/DSTPORT 表示代理侧绑定地址；CONNECT 成功时常为 0.0.0.0:0。</p>
 */
public interface Socks4CommandResponse extends Socks4Message {

    /**
     * Returns the status of this response.
     *
     * <p>应答状态码，见 {@link Socks4CommandStatus}（如 0x5a 成功）。</p>
     */
    Socks4CommandStatus status();

    /**
     * Returns the {@code DSTIP} field of this response.
     *
     * <p>应答中的绑定/目标 IPv4 地址字段。</p>
     */
    String dstAddr();

    /**
     * Returns the {@code DSTPORT} field of this response.
     *
     * <p>应答中的绑定/目标端口字段。</p>
     */
    int dstPort();
}
