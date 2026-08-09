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

import io.reactivex.rxjava4.core.Observer;

/**
 * Observable 侧队列排空契约：查询取消/完成/错误状态，
 * 管理 wip 计数并向 Observer 转发元素。
 *
 * @param <T> 队列/上游元素类型
 * @param <U> 下游接收类型
 */
public interface ObservableQueueDrain<T, U> {

    /** 是否已取消订阅。 */
    boolean cancelled();

    /** 上游是否已完成（正常或错误）。 */
    boolean done();

    /** 上游错误（未完成时为 null）。 */
    Throwable error();

    /** CAS 进入 drain 临界区（wip 0→1）。 */
    boolean enter();

    /**
     * 将 wip 计数增加 m（通常 drain 结束时减回）。
     * @param m 增量
     * @return 增加后的 wip 值
     */
    int leave(int m);

    /**
     * 向 Observer 发射 value（实现类决定背压/丢弃策略）。
     * @param observer 下游 Observer
     * @param value 待发射值
     */
    void accept(Observer<? super U> observer, T value);
}
