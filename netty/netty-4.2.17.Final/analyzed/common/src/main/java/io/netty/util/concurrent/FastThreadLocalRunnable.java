/*
* Copyright 2017 The Netty Project
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
package io.netty.util.concurrent;

import io.netty.util.internal.ObjectUtil;

/**
 * 包装普通 {@link Runnable}，在线程任务结束时自动调用 {@link FastThreadLocal#removeAll()}。
 *
 * <p>由 {@link DefaultThreadFactory} 和 {@link FastThreadLocalThread} 构造器注入，
 * 防止 FastThreadLocal 在线程池复用时泄漏。</p>
 */
final class FastThreadLocalRunnable implements Runnable {
    private final Runnable runnable;

    private FastThreadLocalRunnable(Runnable runnable) {
        this.runnable = ObjectUtil.checkNotNull(runnable, "runnable");
    }

    /** 执行任务，finally 中清理当前线程所有 FastThreadLocal。 */
    @Override
    public void run() {
        try {
            runnable.run();
        } finally {
            FastThreadLocal.removeAll();
        }
    }

    /** 若尚未包装则创建 {@link FastThreadLocalRunnable}，避免重复包装。 */
    static Runnable wrap(Runnable runnable) {
        return runnable instanceof FastThreadLocalRunnable ? runnable : new FastThreadLocalRunnable(runnable);
    }
}
