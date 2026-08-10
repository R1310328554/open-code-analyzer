/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.util.internal;

import java.util.Iterator;

/**
 * 只读 {@link Iterator} 包装器，{@link #remove} 恒抛 {@link UnsupportedOperationException}。
 */
public final class ReadOnlyIterator<T> implements Iterator<T> {
    /** 被包装的底层迭代器。 */
    private final Iterator<? extends T> iterator;

    /** 构造只读视图，iterator 不可为 null。 */
    public ReadOnlyIterator(Iterator<? extends T> iterator) {
        this.iterator = ObjectUtil.checkNotNull(iterator, "iterator");
    }

    /** 委托底层迭代器。 */
    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /** 委托底层迭代器返回下一元素。 */
    @Override
    public T next() {
        return iterator.next();
    }

    /** 只读迭代器禁止删除。 */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("read-only");
    }
}
