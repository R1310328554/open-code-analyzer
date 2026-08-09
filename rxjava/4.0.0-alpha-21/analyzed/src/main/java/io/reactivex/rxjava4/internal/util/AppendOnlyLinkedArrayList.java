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

import io.reactivex.rxjava4.core.Observer;
import io.reactivex.rxjava4.functions.*;

/**
 * 仅支持追加与消费的链式数组列表实现。
 *
 * @param <T> 值类型
 */
public class AppendOnlyLinkedArrayList<T> {
    final int capacity;
    final Object[] head;
    Object[] tail;
    int offset;

    /**
     * 构造空列表，并指定每个链段的容量。
     * @param capacity 每个链段的容量
     */
    public AppendOnlyLinkedArrayList(int capacity) {
        this.capacity = capacity;
        this.head = new Object[capacity + 1];
        this.tail = head;
    }

    /**
     * 向列表追加非 null 值。
     * <p>请勿向列表添加 null！
     * @param value 要追加的值
     */
    public void add(T value) {
        final int c = capacity;
        int o = offset;
        if (o == c) {
            Object[] next = new Object[c + 1];
            tail[c] = next;
            tail = next;
            o = 0;
        }
        tail[o] = value;
        offset = o + 1;
    }

    /**
     * 将值设为列表首元素。
     * @param value 要设置的值
     */
    public void setFirst(T value) {
        head[0] = value;
    }

    /**
     * 不抛出受检异常的谓词接口。
     *
     * @param <T> 值类型
     */
    public interface NonThrowingPredicate<T> extends Predicate<T> {
        @Override
        boolean test(T t);
    }

    /**
     * 遍历数组元素，直到遇到 null 或给定谓词返回 true。
     * @param consumer 值消费者；返回 true 时终止 forEach
     */
    @SuppressWarnings("unchecked")
    public void forEachWhile(NonThrowingPredicate<? super T> consumer) {
        Object[] a = head;
        final int c = capacity;
        while (a != null) {
            for (int i = 0; i < c; i++) {
                Object o = a[i];
                if (o == null) {
                    break;
                }
                if (consumer.test((T)o)) {
                    return;
                }
            }
            a = (Object[])a[c];
        }
    }

    /**
     * 将内容解释为 NotificationLite 对象并调用相应 Subscriber 方法。
     * 
     * @param <U> 目标类型
     * @param subscriber 接收事件的 subscriber
     * @return 若已到达终止事件则为 true
     */
    public <U> boolean accept(Subscriber<? super U> subscriber) {
        Object[] a = head;
        final int c = capacity;
        while (a != null) {
            for (int i = 0; i < c; i++) {
                Object o = a[i];
                if (o == null) {
                    break;
                }

                if (NotificationLite.acceptFull(o, subscriber)) {
                    return true;
                }
            }
            a = (Object[])a[c];
        }
        return false;
    }

    /**
     * 将内容解释为 NotificationLite 对象并调用相应 Observer 方法。
     * 
     * @param <U> 目标类型
     * @param observer 接收事件的 observer
     * @return 若已到达终止事件则为 true
     */
    public <U> boolean accept(Observer<? super U> observer) {
        Object[] a = head;
        final int c = capacity;
        while (a != null) {
            for (int i = 0; i < c; i++) {
                Object o = a[i];
                if (o == null) {
                    break;
                }

                if (NotificationLite.acceptFull(o, observer)) {
                    return true;
                }
            }
            a = (Object[])a[c];
        }
        return false;
    }

    /**
     * 遍历数组元素，直到遇到 null 或给定谓词返回 true。
     * @param <S> 额外状态类型
     * @param state 传入消费者的额外状态
     * @param consumer 值消费者；返回 true 时终止 forEach
     * @throws Throwable 谓词抛出时
     */
    @SuppressWarnings("unchecked")
    public <S> void forEachWhile(S state, BiPredicate<? super S, ? super T> consumer) throws Throwable {
        Object[] a = head;
        final int c = capacity;
        for (;;) {
            for (int i = 0; i < c; i++) {
                Object o = a[i];
                if (o == null) {
                    return;
                }
                if (consumer.test(state, (T)o)) {
                    return;
                }
            }
            a = (Object[])a[c];
            if (a == null) {
                return;
            }
        }
    }
}
