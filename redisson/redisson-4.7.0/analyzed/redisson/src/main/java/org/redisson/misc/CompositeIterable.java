/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 将多个 {@link Iterable} 顺序拼接为一个可迭代集合。
 * <p>
 * 支持 List 或可变参数两种构造方式；
 * {@link #iterator()} 返回 {@link CompositeIterator} 实现顺序遍历。
 */
public class CompositeIterable<T> implements Iterable<T> {

    /** 以 List 形式保存的子 Iterable 集合。 */
    private List<Iterable<T>> iterablesList;
    /** 以数组形式保存的子 Iterable（与 iterablesList 二选一）。 */
    private Iterable<T>[] iterables;
    /** 遍历时的最大元素数；0 表示无限制。 */
    private int limit;

    /** 用 Iterable 列表构造，不设上限。 */
    public CompositeIterable(List<Iterable<T>> iterables) {
        this.iterablesList = iterables;
    }

    /** 用 Iterable 列表与元素上限构造。 */
    public CompositeIterable(List<Iterable<T>> iterables, int limit) {
        this.iterablesList = iterables;
        this.limit = limit;
    }

    /** 用可变参数 Iterable 构造。 */
    public CompositeIterable(Iterable<T>... iterables) {
        this.iterables = iterables;
    }

    /** 拷贝构造，共享底层 Iterable 引用。 */
    public CompositeIterable(CompositeIterable<T> iterable) {
        this.iterables = iterable.iterables;
        this.iterablesList = iterable.iterablesList;
    }

    /** 收集各子 Iterable 的 Iterator 并包装为 {@link CompositeIterator}。 */
    @Override
    public Iterator<T> iterator() {
        List<Iterator<T>> iterators = new ArrayList<Iterator<T>>();
        if (iterables != null) {
            for (Iterable<T> iterable : iterables) {
                iterators.add(iterable.iterator());
            }
        } else {
            for (Iterable<T> iterable : iterablesList) {
                iterators.add(iterable.iterator());
            }
        }
        Iterator<Iterator<T>>  listIterator = iterators.iterator();
        return new CompositeIterator<T>(listIterator, limit);
    }
}
