/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.proxy.remoting.protocol.remoting;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.function.Supplier;
import org.apache.rocketmq.proxy.remoting.protocol.ProtocolHandler;
import org.apache.rocketmq.remoting.netty.NettyDecoder;
import org.apache.rocketmq.remoting.netty.NettyEncoder;
import org.apache.rocketmq.remoting.netty.NettyRemotingServer;
import org.apache.rocketmq.remoting.netty.RemotingCodeDistributionHandler;

/**
 * 经典 Remoting 协议处理器：向管道注入编解码与业务分发处理器。
 */
public class RemotingProtocolHandler implements ProtocolHandler {

    /** {@link NettyEncoder} 懒加载供应器。 */
    private final Supplier<NettyEncoder> encoderSupplier;
    /** Remoting 请求码分发处理器供应器。 */
    private final Supplier<RemotingCodeDistributionHandler> remotingCodeDistributionHandlerSupplier;
    /** 连接生命周期管理处理器供应器。 */
    private final Supplier<NettyRemotingServer.NettyConnectManageHandler> connectionManageHandlerSupplier;
    /** Remoting 服务端业务处理器供应器。 */
    private final Supplier<NettyRemotingServer.NettyServerHandler> serverHandlerSupplier;

    /** 注入各 Remoting 管道组件的供应器。 */
    public RemotingProtocolHandler(Supplier<NettyEncoder> encoderSupplier,
        Supplier<RemotingCodeDistributionHandler> remotingCodeDistributionHandlerSupplier,
        Supplier<NettyRemotingServer.NettyConnectManageHandler> connectionManageHandlerSupplier,
        Supplier<NettyRemotingServer.NettyServerHandler> serverHandlerSupplier) {
        this.encoderSupplier = encoderSupplier;
        this.remotingCodeDistributionHandlerSupplier = remotingCodeDistributionHandlerSupplier;
        this.connectionManageHandlerSupplier = connectionManageHandlerSupplier;
        this.serverHandlerSupplier = serverHandlerSupplier;
    }

    @Override
    /** 作为兜底协议，始终返回 true。 */
    public boolean match(ByteBuf in) {
        return true;
    }

    @Override
    /** 向管道末尾追加编码器、解码器与连接/业务处理器。 */
    public void config(ChannelHandlerContext ctx, ByteBuf msg) {
        ctx.pipeline().addLast(
            this.encoderSupplier.get(),
            new NettyDecoder(),
            this.remotingCodeDistributionHandlerSupplier.get(),
            this.connectionManageHandlerSupplier.get(),
            this.serverHandlerSupplier.get()
        );
    }
}
