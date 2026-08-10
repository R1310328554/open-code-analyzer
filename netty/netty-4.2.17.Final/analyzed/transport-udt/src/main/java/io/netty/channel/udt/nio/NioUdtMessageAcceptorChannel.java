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
 * Message Channel Acceptor for UDT Datagrams.
 * <p>UDT 消息（{@link TypeUDT#DATAGRAM}）Acceptor：监听入站连接， accept 后创建 {@link NioUdtMessageConnectorChannel}。 Pipeline 中须使用 {@link UdtMessage} 收发。</p>
 *
 * @deprecated The UDT transport is no longer maintained and will be removed.
 */
@Deprecated
public class NioUdtMessageAcceptorChannel extends NioUdtAcceptorChannel {

    /** 创建 DATAGRAM 类型的 UDT 消息 Acceptor */
    public NioUdtMessageAcceptorChannel() {
        super(TypeUDT.DATAGRAM);
    }

    @Override
    /** accept 得到的套接字包装为 {@link NioUdtMessageConnectorChannel} */
    protected UdtChannel newConnectorChannel(SocketChannelUDT channelUDT) {
        return new NioUdtMessageConnectorChannel(this, channelUDT);
    }
}
