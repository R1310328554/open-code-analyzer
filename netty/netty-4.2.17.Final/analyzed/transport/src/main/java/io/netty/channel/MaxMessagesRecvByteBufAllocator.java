/*
 * Copyright 2015 The Netty Project
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
package io.netty.channel;

/**
 * 限制 EventLoop 单次读循环中尝试读取操作次数的 {@link RecvByteBufAllocator}。
 * <p>
 * 当 EventLoop 触发读操作时，本接口控制最多读取多少条消息后才结束本次读循环，
 * 从而避免一次 select 唤醒后长时间占用线程。
 * </p>
 */
public interface MaxMessagesRecvByteBufAllocator extends RecvByteBufAllocator {
    /**
     * 返回每次读循环允许读取的最大消息数。
     * <p>
     * 每次成功读取并触发
     * {@link ChannelInboundHandler#channelRead(ChannelHandlerContext, Object) channelRead()} 计为一条消息。
     * 若返回值大于 1，EventLoop 可能在同一次读循环中多次尝试读取以凑齐多条消息。
     * </p>
     */
    int maxMessagesPerRead();

    /**
     * 设置每次读循环允许读取的最大消息数。
     * <p>
     * 若 {@code maxMessagesPerRead} 大于 1，EventLoop 可能在同一次读循环中多次尝试读取。
     * </p>
     *
     * @param maxMessagesPerRead 单次读循环的消息上限
     */
    MaxMessagesRecvByteBufAllocator maxMessagesPerRead(int maxMessagesPerRead);
}
