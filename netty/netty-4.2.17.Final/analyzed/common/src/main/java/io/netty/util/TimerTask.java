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
package io.netty.util;

import java.util.concurrent.TimeUnit;

/**
 * A task which is executed after the delay specified with
 * {@link Timer#newTimeout(TimerTask, long, TimeUnit)}.
 *
 * <p>定时任务回调：由 {@link Timer} 在到期时于后台线程调用 {@link #run(Timeout)}。</p>
 */
public interface TimerTask {

    /**
     * Executed after the delay specified with
     * {@link Timer#newTimeout(TimerTask, long, TimeUnit)}.
     *
     * @param timeout a handle which is associated with this task
     *
     * <p>延迟到期后执行；{@code timeout} 可用于取消或判断是否已过期。</p>
     */
    void run(Timeout timeout) throws Exception;

    /**
     * Called for {@link TimerTask}s that are successfully canceled via {@link Timeout#cancel()}. Overriding this
     * method allows to for example run some cleanup.
     *
     * @param timeout a handle which is associated with this task
     *
     * <p>通过 {@link Timeout#cancel()} 成功取消时回调，可用于释放资源等清理逻辑；默认空实现。</p>
     */
    default void cancelled(Timeout timeout) {
        // By default do nothing.
    }
}
