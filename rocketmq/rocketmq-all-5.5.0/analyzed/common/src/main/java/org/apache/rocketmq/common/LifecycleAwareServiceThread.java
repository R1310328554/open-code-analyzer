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
package org.apache.rocketmq.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 可感知启动完成的服务线程：run 开始时置 started 并通知 awaitStarted 等待者。
 */
public abstract class LifecycleAwareServiceThread extends ServiceThread {

    /** 线程是否已进入 run 逻辑。 */
    private final AtomicBoolean started = new AtomicBoolean(false);

    /** 标记已启动并唤醒等待者，再执行 run0。 */
    @Override
    public void run() {
        started.set(true);
        synchronized (started) {
            started.notifyAll();
        }

        run0();
    }

    /** 子类实现的实际运行逻辑。 */
    public abstract void run0();

    /**
     * 等待线程进入 run（考虑虚假唤醒）。
     *
     * @param timeout 最长等待毫秒数
     * @throws InterruptedException 被中断时
     */
    public void awaitStarted(long timeout) throws InterruptedException {
        long expire = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
        synchronized (started) {
            while (!started.get()) {
                long duration = expire - System.nanoTime();
                if (duration < TimeUnit.MILLISECONDS.toNanos(1)) {
                    break;
                }
                started.wait(TimeUnit.NANOSECONDS.toMillis(duration));
            }
        }
    }
}
