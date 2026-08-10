/*
 * Copyright 2013 The Netty Project
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

import java.util.concurrent.Executor;

/**
 * {@link Executor} which execute tasks in the callers thread.
 *
 * <p>在调用线程中同步执行任务的 {@link Executor} 单例，无队列、无线程切换。
 * 适用于测试或必须立即执行且无需 EventExecutor 语义的场景。</p>
 */
public final class ImmediateExecutor implements Executor {
    /** 全局单例。 */
    public static final ImmediateExecutor INSTANCE = new ImmediateExecutor();

    private ImmediateExecutor() {
        // use static instance — 私有构造，强制使用单例
    }

    @Override
    public void execute(Runnable command) {
        // 直接在调用线程运行，不捕获异常
        ObjectUtil.checkNotNull(command, "command").run();
    }
}
