/*
 * Copyright 2016 The Netty Project
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

import io.netty.util.IntSupplier;

/**
 * select 循环策略接口。
 * <p>
 * 用于控制 EventLoop 在 I/O 多路复用时的行为：例如当已有就绪事件或待处理任务时，
 * 可跳过或延迟阻塞式 select，以降低延迟。
 * </p>
 */
public interface SelectStrategy {

    /** 表示下一步应执行阻塞式 select。 */
    int SELECT = -1;
    /** 表示应重试 I/O 循环，不立即进入阻塞 select。 */
    int CONTINUE = -2;
    /** 表示以非阻塞方式 poll 新事件。 */
    int BUSY_WAIT = -3;

    /**
     * 根据当前状态决定下一次 select 相关操作。
     *
     * @param selectSupplier 封装 select 调用结果的供应器
     * @param hasTasks       是否有待处理任务
     * @return {@link #SELECT} 表示下一步阻塞 select；{@link #CONTINUE} 表示跳过 select 回到 I/O 循环；
     *         任意 {@code >= 0} 的值表示仍有工作待处理
     */
    int calculateStrategy(IntSupplier selectSupplier, boolean hasTasks) throws Exception;
}
