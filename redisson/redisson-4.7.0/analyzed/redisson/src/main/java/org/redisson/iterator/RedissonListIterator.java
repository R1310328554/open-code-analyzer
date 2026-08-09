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
package org.redisson.iterator;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Redisson List 的 {@link ListIterator} 实现。
 * <p>
 * 通过 {@link #getValue(int)} 等抽象方法访问远程 List；
 * 维护当前索引与 last-return 状态，支持 next/previous/set/add/remove。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public abstract class RedissonListIterator<V> implements ListIterator<V> {

    /** 缓存 {@link #hasPrevious()} 预取的上一元素。 */
    private V prevCurrentValue;
    /** 缓存 {@link #hasNext()} 预取的下一元素。 */
    private V nextCurrentValue;
    /** 最近一次 next/previous 返回的元素，供 remove/set 使用。 */
    private V currentValueHasRead;
    /** 当前迭代位置（nextIndex - 1）。 */
    private int currentIndex;
    /** 为 true 表示结构已变，禁止 set；remove 后也会置 true。 */
    private boolean hasBeenModified = true;

    /** @param startIndex 起始 nextIndex（构造时 currentIndex = startIndex - 1） */
    public RedissonListIterator(int startIndex) {
        currentIndex = startIndex - 1;
    }

    /** 读取指定下标元素；不存在时返回 null。 */
    public abstract V getValue(int index);

    /** 删除指定下标元素并返回被删值。 */
    public abstract V remove(int index);

    /** 原地替换指定下标元素（不移动其他元素）。 */
    public abstract void fastSet(int index, V value);

    /** 在指定下标插入元素。 */
    public abstract void add(int index, V value);

    /** 预取并缓存 index+1 处元素，判断是否存在下一项。 */
    @Override
    public boolean hasNext() {
        V val = getValue(currentIndex + 1);
        if (val != null) {
            nextCurrentValue = val;
        }
        return val != null;
    }

    /** 前进一位并返回当前元素；无元素时抛 {@link NoSuchElementException}。 */
    @Override
    public V next() {
        if (nextCurrentValue == null && !hasNext()) {
            throw new NoSuchElementException("No such element at index " + currentIndex);
        }
        currentIndex++;
        currentValueHasRead = nextCurrentValue;
        nextCurrentValue = null;
        hasBeenModified = false;
        return currentValueHasRead;
    }

    /** 删除最近一次 next/previous 返回的元素。 */
    @Override
    public void remove() {
        if (currentValueHasRead == null) {
            throw new IllegalStateException("Neither next nor previous have been called");
        }
        if (hasBeenModified) {
            throw new IllegalStateException("Element been already deleted");
        }
        remove(currentIndex);
        currentIndex--;
        hasBeenModified = true;
        currentValueHasRead = null;
    }

    /** 预取 currentIndex 处元素，判断是否存在上一项。 */
    @Override
    public boolean hasPrevious() {
        if (currentIndex < 0) {
            return false;
        }
        V val = getValue(currentIndex);
        if (val != null) {
            prevCurrentValue = val;
        }
        return val != null;
    }

    /** 后退一位并返回元素。 */
    @Override
    public V previous() {
        if (prevCurrentValue == null && !hasPrevious()) {
            throw new NoSuchElementException("No such element at index " + currentIndex);
        }
        currentIndex--;
        hasBeenModified = false;
        currentValueHasRead = prevCurrentValue;
        prevCurrentValue = null;
        return currentValueHasRead;
    }

    /** 下一元素的 List 下标。 */
    @Override
    public int nextIndex() {
        return currentIndex + 1;
    }

    /** 上一元素的 List 下标。 */
    @Override
    public int previousIndex() {
        return currentIndex;
    }

    /** 替换最近一次 next/previous 返回的元素。 */
    @Override
    public void set(V e) {
        if (hasBeenModified) {
            throw new IllegalStateException();
        }

        fastSet(currentIndex, e);
    }

    /** 在当前位置之后插入元素并前进索引。 */
    @Override
    public void add(V e) {
        add(currentIndex + 1, e);
        currentIndex++;
        hasBeenModified = true;
    }

}
