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

package io.reactivex.rxjava4.internal.virtual;

import java.io.Serial;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 虚拟线程挂起/恢复原语：await 时 park，resume 时 unpark 或置 READY。
 * 供 FlowableVirtual* 算子协调背压。
 * @since 4.0.0
 */
public class VirtualResumable extends AtomicReference<Object> {

    @Serial
    private static final long serialVersionUID = -3462467580179834124L;

    /** 表示 await 可直接通过，无需 park。 */
    static final Object READY = "Ready";

    /**
     * 等待 resume：无 READY 时 CAS 登记当前线程并 LockSupport.park。
     * 若已 READY 则立即返回；退出时 clear 状态。
     */
    public final void await() {
        Thread toUnpark = Thread.currentThread();

        for (;;) {
            var current = get();
            if (current == READY) {
                break;
            }

            if (current != null && current != toUnpark) {
                throw new IllegalStateException("Only one (Virtual)Thread can await this VirtualResumable!");
            }

            if (compareAndSet(null, toUnpark)) {
                LockSupport.park();
                // we don't just break here because park() can wake up spuriously
                // if we got a proper resume, get() == READY and the loop will quit above
            }
        }
        // clear the resume indicator so that the next await call will park without a resume()
        clear();
    }

    /** getAndSet(null)，清除 READY 或 parked 线程引用。 */
    public final void clear() {
        getAndSet(null);
    }

    /**
     * 触发恢复：置 READY 并对已 park 的线程 unpark。
     * 可多次、多线程调用；与 await 之间不保证完整内存屏障。
     */
    public final void resume() {
        if (get() != READY) {
            var old = getAndSet(READY);
            if (old != READY) {
                LockSupport.unpark((Thread)old);
            }
        }
    }
}