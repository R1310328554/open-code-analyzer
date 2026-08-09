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
package org.redisson.client.protocol.decoder;

/**
 * 列表迭代单步结果：当前元素及其后剩余元素个数。
 *
 * @author Nikita Koksharov
 *
 * @param <V> value type
 */
public class ListIteratorResult<V> {

    /** 当前迭代的列表元素。 */
    private final V element;
    /** 该元素之后列表中仍有的元素数量。 */
    private final long size;

    /** 构造一步迭代快照。 */
    public ListIteratorResult(V element, long size) {
        super();
        this.element = element;
        this.size = size;
    }

    /** 返回当前元素。 */
    public V getElement() {
        return element;
    }

    /** 返回剩余元素个数（不含当前元素）。 */
    public long getSize() {
        return size;
    }

}
