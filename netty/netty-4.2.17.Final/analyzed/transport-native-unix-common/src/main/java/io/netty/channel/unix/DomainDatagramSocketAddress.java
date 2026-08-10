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
package io.netty.channel.unix;

import io.netty.util.CharsetUtil;

/**
 * Act as special {@link DomainSocketAddress} to be able to easily pass all needed data from JNI without the need
 * to create more objects then needed.
 * <p>JNI 接收路径专用地址：在对端路径之外附带本次 {@code recvmsg} 字节数与本地绑定地址， 减少额外对象分配。</p>
 * <p>
 * <strong>Internal usage only!</strong>
 */
public final class DomainDatagramSocketAddress extends DomainSocketAddress {

    private static final long serialVersionUID = -5925732678737768223L;

    /** 接收时本地绑定地址（可为 {@code null}） */
    private final DomainDatagramSocketAddress localAddress;
    // JNI 返回的本次读取字节数
    /** {@code recvmsg} 实际读到的字节数 */
    private final int receivedAmount;

    /** 由 JNI 字节路径构造对端地址并附带元数据 */
    public DomainDatagramSocketAddress(byte[] socketPath, int receivedAmount,
                                       DomainDatagramSocketAddress localAddress) {
        super(new String(socketPath, CharsetUtil.UTF_8));
        this.localAddress = localAddress;
        this.receivedAmount = receivedAmount;
    }

    /** 返回本地绑定地址（嵌套结构，根节点可为 {@code null}） */
    public DomainDatagramSocketAddress localAddress() {
        return localAddress;
    }

    /** 返回本次接收的字节数 */
    public int receivedAmount() {
        return receivedAmount;
    }
}
