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
package org.apache.rocketmq.remoting.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 信号量一次性释放包装：保证 {@link java.util.concurrent.Semaphore#release()} 至多执行一次，
 * 避免异步回调重复归还许可导致计数膨胀。
 */
public class SemaphoreReleaseOnlyOnce {
    /** 是否已释放过许可的原子标记。 */
    private final AtomicBoolean released = new AtomicBoolean(false);
    private final Semaphore semaphore;

    /** 绑定待释放的目标信号量。 */
    public SemaphoreReleaseOnlyOnce(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    /** 首次调用时释放一次许可，后续调用忽略。 */
    public void release() {
        if (this.semaphore != null) {
            if (this.released.compareAndSet(false, true)) {
                this.semaphore.release();
            }
        }
    }

    /** 返回内部持有的信号量实例。 */
    public Semaphore getSemaphore() {
        return semaphore;
    }
}
