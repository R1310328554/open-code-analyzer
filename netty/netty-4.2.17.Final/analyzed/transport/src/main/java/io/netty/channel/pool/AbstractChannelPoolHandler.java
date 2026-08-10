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
package io.netty.channel.pool;

import io.netty.channel.Channel;

/**
 * A skeletal {@link ChannelPoolHandler} implementation.
 * <p>{@link ChannelPoolHandler} 的骨架实现：默认对 acquire/release 为空操作。</p>
 */
public abstract class AbstractChannelPoolHandler implements ChannelPoolHandler {

    /**
     * NOOP implementation, sub-classes may override this.
     * <p>空实现；子类可覆盖以在 channel 被借出时做初始化。</p>
     *
     * {@inheritDoc}
     */
    @Override
    public void channelAcquired(@SuppressWarnings("unused") Channel ch) throws Exception {
        // NOOP
    }

    /**
     * NOOP implementation, sub-classes may override this.
     * <p>空实现；子类可覆盖以在 channel 归还池时做清理。</p>
     *
     * {@inheritDoc}
     */
    @Override
    public void channelReleased(@SuppressWarnings("unused") Channel ch) throws Exception {
        // NOOP
    }
}
