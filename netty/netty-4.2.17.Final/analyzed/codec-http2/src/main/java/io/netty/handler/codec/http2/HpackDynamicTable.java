/*
 * Copyright 2015 The Netty Project
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

/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.netty.handler.codec.http2;

import static io.netty.handler.codec.http2.Http2CodecUtil.MAX_HEADER_TABLE_SIZE;
import static io.netty.handler.codec.http2.Http2CodecUtil.MIN_HEADER_TABLE_SIZE;

/**
 * HPACK 动态表：环形队列存储最近出现的头字段，供索引引用以压缩重复头。
 * <p>索引 1 为最新条目，{@code length()} 为最旧；容量由 SETTINGS {@code HEADER_TABLE_SIZE} 约束，
 * 超出时从尾部（最旧）逐条驱逐直至有空间。
 */
final class HpackDynamicTable {

    // 环形队列存放头字段条目
    HpackHeaderField[] hpackHeaderFields;
    /** 下一写入位置（队列头，最新条目将写入此处）。 */
    int head;
    /** 最旧条目所在位置（驱逐时从此处移除）。 */
    int tail;
    /** 当前表内所有条目 size() 之和。 */
    private long size;
    /** 允许的最大表容量（字节）。 */
    private long capacity = -1; // ensure setCapacity creates the array

    /**
     * 创建指定初始容量的动态表。
     */
    HpackDynamicTable(long initialCapacity) {
        setCapacity(initialCapacity);
    }

    /**
     * Return the number of header fields in the dynamic table.
     */
    public int length() {
        int length;
        if (head < tail) {
            length = hpackHeaderFields.length - tail + head;
        } else {
            length = head - tail;
        }
        return length;
    }

    /**
     * Return the current size of the dynamic table. This is the sum of the size of the entries.
     */
    public long size() {
        return size;
    }

    /**
     * Return the maximum allowable size of the dynamic table.
     */
    public long capacity() {
        return capacity;
    }

    /**
     * Return the header field at the given index. The first and newest entry is always at index 1,
     * and the oldest entry is at the index length().
     */
    public HpackHeaderField getEntry(int index) {
        if (index <= 0 || index > length()) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + length());
        }
        int i = head - index;
        if (i < 0) {
            return hpackHeaderFields[i + hpackHeaderFields.length];
        } else {
            return hpackHeaderFields[i];
        }
    }

    /**
     * 将头字段插入动态表；若超出容量则驱逐最旧条目，单条过大则清空整表。
     */
    public void add(HpackHeaderField header) {
        int headerSize = header.size();
        if (headerSize > capacity) {
            clear();
            return;
        }
        while (capacity - size < headerSize) {
            remove();
        }
        hpackHeaderFields[head++] = header;
        size += headerSize;
        if (head == hpackHeaderFields.length) {
            head = 0;
        }
    }

    /**
     * Remove and return the oldest header field from the dynamic table.
     */
    public HpackHeaderField remove() {
        HpackHeaderField removed = hpackHeaderFields[tail];
        if (removed == null) {
            return null;
        }
        size -= removed.size();
        hpackHeaderFields[tail++] = null;
        if (tail == hpackHeaderFields.length) {
            tail = 0;
        }
        return removed;
    }

    /**
     * Remove all entries from the dynamic table.
     */
    public void clear() {
        while (tail != head) {
            hpackHeaderFields[tail++] = null;
            if (tail == hpackHeaderFields.length) {
                tail = 0;
            }
        }
        head = 0;
        tail = 0;
        size = 0;
    }

    /**
     * Set the maximum size of the dynamic table. Entries are evicted from the dynamic table until
     * the size of the table is less than or equal to the maximum size.
     */
    public void setCapacity(long capacity) {
        if (capacity < MIN_HEADER_TABLE_SIZE || capacity > MAX_HEADER_TABLE_SIZE) {
            throw new IllegalArgumentException("capacity is invalid: " + capacity);
        }
        // initially capacity will be -1 so init won't return here
        if (this.capacity == capacity) {
            return;
        }
        this.capacity = capacity;

        if (capacity == 0) {
            clear();
        } else {
            // initially size will be 0 so remove won't be called
            while (size > capacity) {
                remove();
            }
        }

        int maxEntries = (int) (capacity / HpackHeaderField.HEADER_ENTRY_OVERHEAD);
        if (capacity % HpackHeaderField.HEADER_ENTRY_OVERHEAD != 0) {
            maxEntries++;
        }

        // check if capacity change requires us to reallocate the array
        if (hpackHeaderFields != null && hpackHeaderFields.length == maxEntries) {
            return;
        }

        HpackHeaderField[] tmp = new HpackHeaderField[maxEntries];

        // initially length will be 0 so there will be no copy
        int len = length();
        if (hpackHeaderFields != null) {
            int cursor = tail;
            for (int i = 0; i < len; i++) {
                HpackHeaderField entry = hpackHeaderFields[cursor++];
                tmp[i] = entry;
                if (cursor == hpackHeaderFields.length) {
                    cursor = 0;
                }
            }
        }

        tail = 0;
        head = tail + len;
        hpackHeaderFields = tmp;
    }
}
