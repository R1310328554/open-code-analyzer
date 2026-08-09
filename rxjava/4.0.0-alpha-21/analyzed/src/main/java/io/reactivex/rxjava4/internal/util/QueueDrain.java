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

package io.reactivex.rxjava4.internal.util;

import static java.util.concurrent.Flow.*;

/**
 * Flowable 侧队列排空契约：除 Observable 版状态外，
 * 还跟踪 requested/produced 背压计数。
 *
 * @param <T> 队列元素类型
 * @param <U> 下游接收类型
 */
public interface QueueDrain<T, U> {

    /** 是否已取消。 */
    boolean cancelled();

    /** 上游是否结束。 */
    boolean done();

    /** 上游错误。 */
    Throwable error();

    /** 尝试进入 drain（wip CAS）。 */
    boolean enter();

    /** 当前累计 request 量。 */
    long requested();

    /** 记录已向下游发射 n 个元素并返回新的 produced 计数。 */
    long produced(long n);

    /**
     * wip 计数增加 m。
     * @param m 增量
     * @return 增加后的 wip
     */
    int leave(int m);

    /**
     * 向 Subscriber 发射 value。
     * @param subscriber 下游 Subscriber
     * @param value 待发射值
     * @return 成功转发则 true
     */
    boolean accept(Subscriber<? super U> subscriber, T value);
}
