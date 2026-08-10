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
package io.netty.channel;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;

/**
 * The {@link CompleteChannelFuture} which is failed already.  It is
 * recommended to use {@link Channel#newFailedFuture(Throwable)}
 * instead of calling the constructor of this future.
 * <p>已处于失败状态的 {@link CompleteChannelFuture}。建议通过
 * {@link Channel#newFailedFuture(Throwable)} 创建，而非直接调用本类构造器。</p>
 */
final class FailedChannelFuture extends CompleteChannelFuture {

    /** 失败原因 */
    private final Throwable cause;

    /**
     * Creates a new instance.
     * <p>创建与指定 {@link Channel} 关联、已标记为失败的 future。</p>
     *
     * @param channel the {@link Channel} associated with this future
     * @param cause   the cause of failure
     */
    FailedChannelFuture(Channel channel, EventExecutor executor, Throwable cause) {
        super(channel, executor);
        this.cause = ObjectUtil.checkNotNull(cause, "cause");
    }

    /** 返回失败原因。 */
    @Override
    public Throwable cause() {
        return cause;
    }

    /** 恒为 {@code false}。 */
    @Override
    public boolean isSuccess() {
        return false;
    }

    /** 同步等待并重新抛出失败原因。 */
    @Override
    public ChannelFuture sync() {
        PlatformDependent.throwException(cause);
        return this;
    }

    /** 不可中断地同步并重新抛出失败原因。 */
    @Override
    public ChannelFuture syncUninterruptibly() {
        PlatformDependent.throwException(cause);
        return this;
    }
}
