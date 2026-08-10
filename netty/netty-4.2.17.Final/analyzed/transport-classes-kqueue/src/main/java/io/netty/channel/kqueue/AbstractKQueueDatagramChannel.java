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
package io.netty.channel.kqueue;

import io.netty.channel.Channel;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;

import java.io.IOException;

/**
 * KQueue datagram 通道抽象基类：实现多目标写入与 writeSpinCount 循环。
 * <p>单个 write 失败不中断整批（可写多个远端），见 netty#2665。</p>
 */
abstract class AbstractKQueueDatagramChannel extends AbstractKQueueChannel {

    private static final ChannelMetadata METADATA = new ChannelMetadata(true, 16);

    AbstractKQueueDatagramChannel(Channel parent, BsdSocket fd, boolean active) {
        super(parent, fd, active);
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    protected abstract boolean doWriteMessage(Object msg) throws Exception;

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        int maxMessagesPerWrite = maxMessagesPerWrite();
        while (maxMessagesPerWrite > 0) {
            Object msg = in.current();
            if (msg == null) {
                break;
            }

            try {
                boolean done = false;
                for (int i = config().getWriteSpinCount(); i > 0; --i) {
                    if (doWriteMessage(msg)) {
                        done = true;
                        break;
                    }
                }

                if (done) {
                    in.remove();
                    maxMessagesPerWrite--;
                } else {
                    break;
                }
            } catch (IOException e) {
                maxMessagesPerWrite--;

                // datagram 可写多个远端，单条失败继续写其余（netty#2665）
                in.remove(e);
            }
        }

        // 根据 outbound 是否为空切换写过滤器
        writeFilter(!in.isEmpty());
    }
}
