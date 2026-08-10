/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.monitor.topn;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * 固定容量的小顶堆优先队列，用于维护 TopN 最大元素（O(log N) 插入）。
 * <p>队列满时，仅当新元素优于堆顶（ comparator 意义下更大）才替换堆顶。</p>
 * Fixed size priority queue.
 *
 * @author xiweng.yy
 */
public class FixedSizePriorityQueue<T> {
    
    /** 堆底层数组，长度等于容量上限。 */
    private Object[] elements;
    
    /** 当前堆中元素个数。 */
    private int size;
    
    /** 元素比较器，堆顶为 comparator 意义下的最小值。 */
    private Comparator<T> comparator;
    
    /** 指定容量与比较器创建固定大小优先队列。 */
    public FixedSizePriorityQueue(int capacity, Comparator<T> comparator) {
        elements = new Object[capacity];
        size = 0;
        this.comparator = comparator;
    }
    
    /**
     * 入队：未满则上浮插入；已满时仅当新元素大于堆顶才替换堆顶并下沉，否则忽略。
     *
     * @param element new element.
     */
    public void offer(T element) {
        if (size == elements.length) {
            if (comparator.compare(element, (T) elements[0]) > 0) {
                elements[0] = element;
                siftDown();
            }
        } else {
            elements[size] = element;
            siftUp(size);
            size++;
        }
    }
    
    /** 将 index 处元素向上调整至堆有序。 */
    private void siftUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (comparator.compare((T) elements[index], (T) elements[parentIndex]) > 0) {
                break;
            }
            swap(index, parentIndex);
            index = parentIndex;
        }
    }
    
    /** 从堆顶向下调整至堆有序。 */
    private void siftDown() {
        int index = 0;
        while (index * 2 + 1 < size) {
            int leftChild = index * 2 + 1;
            int rightChild = index * 2 + 2;
            int minChildIndex = leftChild;
            if (rightChild < size
                && comparator.compare((T) elements[rightChild], (T) elements[leftChild]) < 0) {
                minChildIndex = rightChild;
            }
            if (comparator.compare((T) elements[index], (T) elements[minChildIndex]) < 0) {
                break;
            }
            swap(index, minChildIndex);
            index = minChildIndex;
        }
    }
    
    /** 交换堆中两个下标的元素。 */
    private void swap(int i, int j) {
        Object temp = elements[i];
        elements[i] = elements[j];
        elements[j] = temp;
    }
    
    /**
     * 将堆中元素拷贝为链表（不保证排序顺序）。
     *
     * @return list
     */
    public List<T> toList() {
        List<T> result = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            result.add((T) elements[i]);
        }
        return result;
    }
}
