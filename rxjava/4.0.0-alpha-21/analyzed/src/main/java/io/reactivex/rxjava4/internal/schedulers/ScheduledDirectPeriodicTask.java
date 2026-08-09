/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.rxjava4.internal.schedulers;

import io.reactivex.rxjava4.plugins.RxJavaPlugins;

import java.io.Serial;

/**
 * 提交给 ExecutorService 的周期 Runnable 包装，
 * 管理完成/取消与 runner 线程中断策略。
 * @since 2.0.8
 */
public final class ScheduledDirectPeriodicTask extends AbstractDirectTask implements Runnable {

    @Serial
    private static final long serialVersionUID = 1811839108042568751L;

    public ScheduledDirectPeriodicTask(Runnable runnable, boolean interruptOnCancel) {
        super(runnable, interruptOnCancel);
    }

    /** 执行 runnable；异常时 dispose 并通过 RxJavaPlugins 上报。 */
    @Override
    public void run() {
        runner = Thread.currentThread();
        try {
            runnable.run();
            runner = null;
        } catch (Throwable ex) {
            // Exceptions.throwIfFatal(ex); nowhere to go
            dispose();
            runner = null;
            RxJavaPlugins.onError(ex);
            throw ex;
        }
    }
}
