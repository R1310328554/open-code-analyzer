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

package io.reactivex.rxjava4.internal.subscriptions;

import java.io.Serial;
import java.util.Objects;
import java.util.concurrent.atomic.*;

import static java.util.concurrent.Flow.*;

import io.reactivex.rxjava4.internal.util.BackpressureHelper;

/**
 * Subscription 仲裁器：合并 missed request/produced/subscription 变更，
 * 在 drainLoop 中串行应用到当前 actual Subscription。
 */
public class SubscriptionArbiter extends AtomicInteger implements Subscription {

    @Serial
    private static final long serialVersionUID = -2189523197179400958L;

    /** 当前生效的 upstream Subscription，可为 null。 */
    Subscription actual;

    /** 当前累计未转发的 request 量。 */
    long requested;

    final AtomicReference<Subscription> missedSubscription;

    final AtomicLong missedRequested;

    final AtomicLong missedProduced;

    final boolean cancelOnReplace;

    volatile boolean cancelled;

    protected boolean unbounded;

    /** @param cancelOnReplace 替换 Subscription 时是否 cancel 旧值 */
    public SubscriptionArbiter(boolean cancelOnReplace) {
        this.cancelOnReplace = cancelOnReplace;
        missedSubscription = new AtomicReference<>();
        missedRequested = new AtomicLong();
        missedProduced = new AtomicLong();
    }

    /**
     * 设置新 Subscription；有积压 request 则转发。
     * @param s 新 Subscription，非 null（已校验）
     */
    public final void setSubscription(Subscription s) {
        if (cancelled) {
            s.cancel();
            return;
        }

        Objects.requireNonNull(s, "s is null");

        if (get() == 0 && compareAndSet(0, 1)) {
            Subscription a = actual;

            if (a != null && cancelOnReplace) {
                a.cancel();
            }

            actual = s;

            long r = requested;

            if (decrementAndGet() != 0) {
                drainLoop();
            }

            if (r != 0L) {
                s.request(r);
            }

            return;
        }

        Subscription a = missedSubscription.getAndSet(s);
        if (a != null && cancelOnReplace) {
            a.cancel();
        }
        drain();
    }

    /** 累加 request 并 drain；达到 MAX_VALUE 时 unbounded。 */
    @Override
    public final void request(long n) {
        if (SubscriptionHelper.validate(n)) {
            if (unbounded) {
                return;
            }
            if (get() == 0 && compareAndSet(0, 1)) {
                long r = requested;

                if (r != Long.MAX_VALUE) {
                    r = BackpressureHelper.addCap(r, n);
                    requested = r;
                    if (r == Long.MAX_VALUE) {
                        unbounded = true;
                    }
                }
                Subscription a = actual;

                if (decrementAndGet() != 0) {
                    drainLoop();
                }

                if (a != null) {
                    a.request(n);
                }

                return;
            }

            BackpressureHelper.add(missedRequested, n);

            drain();
        }
    }

    /** 扣减已生产数量；负值时 reportMoreProduced。 */
    public final void produced(long n) {
        if (unbounded) {
            return;
        }
        if (get() == 0 && compareAndSet(0, 1)) {
            long r = requested;

            if (r != Long.MAX_VALUE) {
                long u = r - n;
                if (u < 0L) {
                    SubscriptionHelper.reportMoreProduced(u);
                    u = 0;
                }
                requested = u;
            }

            if (decrementAndGet() == 0) {
                return;
            }

            drainLoop();

            return;
        }

        BackpressureHelper.add(missedProduced, n);

        drain();
    }

    /** 置 cancelled 并 drain 取消 actual/missed。 */
    @Override
    public void cancel() {
        if (!cancelled) {
            cancelled = true;

            drain();
        }
    }

    final void drain() {
        if (getAndIncrement() != 0) {
            return;
        }
        drainLoop();
    }

    /** 合并 missed 字段并 request/cancel 当前 actual。 */
    final void drainLoop() {
        int missed = 1;

        long requestAmount = 0L;
        Subscription requestTarget = null;

        for (; ; ) {

            Subscription ms = missedSubscription.get();

            if (ms != null) {
                ms = missedSubscription.getAndSet(null);
            }

            long mr = missedRequested.get();
            if (mr != 0L) {
                mr = missedRequested.getAndSet(0L);
            }

            long mp = missedProduced.get();
            if (mp != 0L) {
                mp = missedProduced.getAndSet(0L);
            }

            Subscription a = actual;

            if (cancelled) {
                if (a != null) {
                    a.cancel();
                    actual = null;
                }
                if (ms != null) {
                    ms.cancel();
                }
            } else {
                long r = requested;
                if (r != Long.MAX_VALUE) {
                    long u = BackpressureHelper.addCap(r, mr);

                    if (u != Long.MAX_VALUE) {
                        long v = u - mp;
                        if (v < 0L) {
                            SubscriptionHelper.reportMoreProduced(v);
                            v = 0;
                        }
                        r = v;
                    } else {
                        r = u;
                    }
                    requested = r;
                }

                if (ms != null) {
                    if (a != null && cancelOnReplace) {
                        a.cancel();
                    }
                    actual = ms;
                    if (r != 0L) {
                        requestAmount = BackpressureHelper.addCap(requestAmount, r);
                        requestTarget = ms;
                    }
                } else if (a != null && mr != 0L) {
                    requestAmount = BackpressureHelper.addCap(requestAmount, mr);
                    requestTarget = a;
                }
            }

            missed = addAndGet(-missed);
            if (missed == 0) {
                if (requestAmount != 0L) {
                    requestTarget.request(requestAmount);
                }
                return;
            }
        }
    }

    /**
     * 是否已进入无界 request 模式。
     * @return unbounded 则 true
     */
    public final boolean isUnbounded() {
        return unbounded;
    }

    /**
     * 是否已 cancel。
     * @return 已取消则 true
     */
    public final boolean isCancelled() {
        return cancelled;
    }
}
