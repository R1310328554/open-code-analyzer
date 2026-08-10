/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.udt.nio;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import io.netty.channel.udt.UdtChannel;

/**
 * Byte Channel Acceptor for UDT Streams.
 * <p>UDT 字节流（{@link TypeUDT#STREAM}）Acceptor：接受连接后创建 {@link NioUdtByteConnectorChannel} 作为已连接的字节流通道。</p>
 *
 * @deprecated The UDT transport is no longer maintained and will be removed.
 */
@Deprecated
public class NioUdtByteAcceptorChannel extends NioUdtAcceptorChannel {

    /** 创建 STREAM 类型的 UDT 字节 Acceptor */
    public NioUdtByteAcceptorChannel() {
        super(TypeUDT.STREAM);
    }

    @Override
    /** 将 accept 得到的套接字包装为 {@link NioUdtByteConnectorChannel} */
    protected UdtChannel newConnectorChannel(SocketChannelUDT channelUDT) {
        return new NioUdtByteConnectorChannel(this, channelUDT);
    }
}
