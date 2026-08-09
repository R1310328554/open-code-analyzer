/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.common.future;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 扩展 {@link FutureTask}，额外保留底层 {@link Runnable} 引用以便外部获取。
 *
 * @param <V> 异步任务结果类型
 */
public class FutureTaskExt<V> extends FutureTask<V> {
    /** 构造时传入的 Runnable；Callable 构造路径下为 null。 */
    private final Runnable runnable;

    /** 基于 Callable 创建 FutureTask 扩展。 */
    public FutureTaskExt(final Callable<V> callable) {
        super(callable);
        this.runnable = null;
    }

    /** 基于 Runnable 与预设结果值创建 FutureTask 扩展。 */
    public FutureTaskExt(final Runnable runnable, final V result) {
        super(runnable, result);
        this.runnable = runnable;
    }

    /** 返回关联的 Runnable，Callable 路径下为 null。 */
    public Runnable getRunnable() {
        return runnable;
    }
}
