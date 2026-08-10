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
import java.util.concurrent.ThreadFactory;

/**
 * 每次 {@link #execute(Runnable)} 都通过 {@link ThreadFactory} 新建线程并立即 start 的简单执行器。
 */
public final class ThreadPerTaskExecutor implements Executor {
    /** 用于创建执行线程的工厂。 */
    private final ThreadFactory threadFactory;

    public ThreadPerTaskExecutor(ThreadFactory threadFactory) {
        this.threadFactory = ObjectUtil.checkNotNull(threadFactory, "threadFactory");
    }

    /** 新建线程执行 command，不池化、不复用。 */
    @Override
    public void execute(Runnable command) {
        threadFactory.newThread(command).start();
    }
}
