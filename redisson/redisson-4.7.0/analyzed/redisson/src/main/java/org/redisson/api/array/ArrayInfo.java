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
package org.redisson.api.array;

import java.io.Serializable;

/**
 * 数组信息对象。
 * <p>
 * 保存数组始终可用的基础信息；仅在请求完整信息时才填充的扩展统计见 {@link ArrayFullInfo}。
 *
 * @author lamnt2008
 *
 */
public class ArrayInfo implements Serializable {

    private static final long serialVersionUID = -5000606611320810658L;

    private long count;
    private long length;
    private long nextInsertIndex;
    private long slices;
    private long directorySize;
    private long superDirectoryEntries;
    private long sliceSize;

    /**
     * 返回数组中存储的元素个数。
     *
     * @return 元素个数
     */
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    /**
     * 返回数组长度。
     *
     * @return 数组长度
     */
    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    /**
     * 返回数组插入操作将使用的下一个索引。
     *
     * @return 下一次插入使用的索引
     */
    public long getNextInsertIndex() {
        return nextInsertIndex;
    }

    public void setNextInsertIndex(long nextInsertIndex) {
        this.nextInsertIndex = nextInsertIndex;
    }

    /**
     * 返回切片数量。
     *
     * @return 切片数量
     */
    public long getSlices() {
        return slices;
    }

    public void setSlices(long slices) {
        this.slices = slices;
    }

    /**
     * 返回目录大小。
     *
     * @return 目录大小
     */
    public long getDirectorySize() {
        return directorySize;
    }

    public void setDirectorySize(long directorySize) {
        this.directorySize = directorySize;
    }

    /**
     * 返回超级目录项数量。
     *
     * @return 超级目录项数量
     */
    public long getSuperDirectoryEntries() {
        return superDirectoryEntries;
    }

    public void setSuperDirectoryEntries(long superDirectoryEntries) {
        this.superDirectoryEntries = superDirectoryEntries;
    }

    /**
     * 返回切片大小。
     *
     * @return 切片大小
     */
    public long getSliceSize() {
        return sliceSize;
    }

    public void setSliceSize(long sliceSize) {
        this.sliceSize = sliceSize;
    }

    @Override
    public String toString() {
        return "ArrayInfo{" +
                "count=" + count +
                ", length=" + length +
                ", nextInsertIndex=" + nextInsertIndex +
                ", slices=" + slices +
                ", directorySize=" + directorySize +
                ", superDirectoryEntries=" + superDirectoryEntries +
                ", sliceSize=" + sliceSize +
                '}';
    }
}
