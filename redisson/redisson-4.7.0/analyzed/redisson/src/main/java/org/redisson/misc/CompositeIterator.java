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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 顺序遍历多个 {@link Iterator}，如同单一迭代器。
 * <p>
 * 当前子迭代器耗尽后自动切换到下一个；
 * 可选 {@code limit} 限制总共访问的元素个数。
 *
 * @author Pepe Lu
 */
public class CompositeIterator<T> implements Iterator<T> {

    /** 子 Iterator 的 Iterator。 */
    private Iterator<Iterator<T>> listIterator;
    /** 当前正在读取的子迭代器。 */
    private Iterator<T> currentIterator;
    /** 最大元素数；0 表示无限制。 */
    private int limit;
    /** 已返回的元素计数。 */
    private int counter;

    /** 用子迭代器列表与上限构造组合迭代器。 */
    public CompositeIterator(Iterator<Iterator<T>> iterators, int limit) {
        listIterator = iterators;
        this.limit = limit;
    }

    /** 判断是否还有下一元素，必要时切换到下一个子迭代器。 */
    @Override
    public boolean hasNext() {
        if (currentIterator == null || !currentIterator.hasNext()) {
            while (listIterator.hasNext()) {
                Iterator<T> iterator = listIterator.next();
                currentIterator = iterator;
                if (iterator.hasNext()) {
                    if (limit == 0) {
                        return true;
                    } else {
                        return limit >= counter + 1;
                    }
                }
            }
            return false;
        }

        if (currentIterator.hasNext()) {
            if (limit == 0) {
                return true;
            } else {
                return limit >= counter + 1;
            }
        }
        return false;
    }

    /** 返回下一元素并递增计数器。 */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        counter++;
        return currentIterator.next();
    }

    /** 从当前子迭代器删除最后返回的元素。 */
    @Override
    public void remove() {
        if (currentIterator == null) {
            throw new IllegalStateException("next() has not yet been called");
        }

        currentIterator.remove();
    }
}
