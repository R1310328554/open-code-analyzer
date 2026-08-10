/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.timer.basic;

import java.util.TimerTask;

import org.keycloak.timer.TimerProvider;

/**
 * {@link TimerProvider.TimerTaskContext} 的基础实现，封装定时任务的可执行体与时间参数。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TimerTaskContextImpl implements TimerProvider.TimerTaskContext {

    /** 任务实际执行的 {@link Runnable}。 */
    private final Runnable runnable;
    /** 底层 {@link TimerTask} 引用。 */
    final TimerTask timerTask;
    /** 首次执行时间（毫秒时间戳）。 */
    private final long startTimeMillis;
    /** 重复执行间隔（毫秒），0 表示仅执行一次。 */
    private final long intervalMillis;

    /** 构造定时任务上下文。 */
    public TimerTaskContextImpl(Runnable runnable, TimerTask timerTask, long startTimeMillis, long intervalMillis) {
        this.runnable = runnable;
        this.timerTask = timerTask;
        this.startTimeMillis = startTimeMillis;
        this.intervalMillis = intervalMillis;
    }

    @Override
    public Runnable getRunnable() {
        return runnable;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    @Override
    public long getIntervalMillis() {
        return intervalMillis;
    }
}
