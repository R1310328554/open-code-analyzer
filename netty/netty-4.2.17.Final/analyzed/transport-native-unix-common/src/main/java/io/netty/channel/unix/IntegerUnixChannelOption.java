/*
 * Copyright 2022 The Netty Project
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

/**
 * A {@link GenericUnixChannelOption} which uses an {@link Integer} as {@code optval}.
 * <p>整型 Unix 套接字选项：optval 为 32 位整数，适用于常见 {@code int} 型 sockopt。</p>
 */
public final class IntegerUnixChannelOption extends GenericUnixChannelOption<Integer> {
    /**
     * Creates a new instance.
     *
     * @param name      the name that is used.
     * @param level     the level.
     * @param optname   the optname.
     * <p>注册具名整型选项供 {@link ChannelOption} 映射使用。</p>
     */
    public IntegerUnixChannelOption(String name, int level, int optname) {
        super(name, level, optname);
    }
}
