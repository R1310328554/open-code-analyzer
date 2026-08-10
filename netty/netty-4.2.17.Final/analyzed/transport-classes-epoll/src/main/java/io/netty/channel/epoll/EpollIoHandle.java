/*
 * Copyright 2024 The Netty Project
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
package io.netty.channel.epoll;

import io.netty.channel.IoHandle;
import io.netty.channel.unix.FileDescriptor;

/**
 * {@link IoHandle} implementation which is using epoll.
 * <p>基于 epoll 的 {@link IoHandle}，暴露底层 {@link FileDescriptor}。</p>
 */
public interface EpollIoHandle extends IoHandle {
    /**
     * Returns the {@link FileDescriptor} that used by this {@link IoHandle}.
     * <p>返回本 handle 注册到 epoll 的文件描述符。</p>
     *
     * @return fd
     */
    FileDescriptor fd();
}
