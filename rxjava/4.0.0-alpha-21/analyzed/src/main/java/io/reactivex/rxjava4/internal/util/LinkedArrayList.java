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

import java.util.*;

/**
 * 结合 ArrayList 与 LinkedList 的列表实现，扩容时避免复制已有元素。
 * <p>
 * 类非 final，便于内嵌以减少对象分配。
 */
public class LinkedArrayList {
    /** 每个数组段的容量。 */
    final int capacityHint;
    /**
     * 非 null 时为链式数组列表头；长度恒为 capacityHint + 1，
     * 末元素为指向下一段的 Object[]。
     */
    Object[] head;
    /** 当前尾段，新元素追加于此。 */
    Object[] tail;
    /**
     * 列表总大小；写入在追加之后（release），
     * 读取时表示可安全读取的元素数（acquire）。
     */
    volatile int size;
    /** 当前尾段下一个可用槽位。 */
    int indexInTail;
    /**
     * 指定每段容量提示的构造函数。
     * @param capacityHint 预期元素数（可超出）
     */
    public LinkedArrayList(int capacityHint) {
        this.capacityHint = capacityHint;
    }
    /**
     * 向列表添加元素。
     * @param o 要添加的对象，允许 null
     */
    public void add(Object o) {
        // 尚无元素，创建首段数组
        if (size == 0) {
            head = new Object[capacityHint + 1];
            tail = head;
            head[0] = o;
            indexInTail = 1;
            size = 1;
        } else
        // 尾段已满，创建新尾段并链接
        if (indexInTail == capacityHint) {
            Object[] t = new Object[capacityHint + 1];
            t[0] = o;
            tail[capacityHint] = t;
            tail = t;
            indexInTail = 1;
            size++;
        } else {
            tail[indexInTail] = o;
            indexInTail++;
            size++;
        }
    }
    /**
     * 返回头段缓冲区；列表为空时为 null。
     * @return 头 Object 数组
     */
    public Object[] head() {
        return head; // NOPMD
    }

    /**
     * 返回列表总大小。
     * @return 列表总大小
     */
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        final int cap = capacityHint;
        final int s = size;
        final List<Object> list = new ArrayList<>(s + 1);

        Object[] h = head();
        int j = 0;
        int k = 0;
        while (j < s) {
            list.add(h[k]);
            j++;
            if (++k == cap) {
                k = 0;
                h = (Object[])h[cap];
            }
        }

        return list.toString();
    }
}
