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

/**
 * Redis 数组完整元信息对象。
 * <p>
 * 继承 {@link ArrayInfo}，在通过 {@code RArray.getFullInfo()} 请求完整信息时返回额外的切片统计字段。
 *
 * @author Nikita Koksharov
 *
 */
public final class ArrayFullInfo extends ArrayInfo {

    private static final long serialVersionUID = -5000606611320810658L;

    private Long denseSlices;
    private Long sparseSlices;
    private Double averageDenseSize;
    private Double averageDenseFill;
    private Double averageSparseSize;

    /**
     * 返回稠密（dense）切片数量。
     *
     * @return 稠密切片数
     */
    public Long getDenseSlices() {
        return denseSlices;
    }

    public void setDenseSlices(Long denseSlices) {
        this.denseSlices = denseSlices;
    }

    /**
     * 返回稀疏（sparse）切片数量。
     *
     * @return 稀疏切片数
     */
    public Long getSparseSlices() {
        return sparseSlices;
    }

    public void setSparseSlices(Long sparseSlices) {
        this.sparseSlices = sparseSlices;
    }

    /**
     * 返回稠密切片的平均大小。
     *
     * @return 稠密切片平均大小
     */
    public Double getAverageDenseSize() {
        return averageDenseSize;
    }

    public void setAverageDenseSize(Double averageDenseSize) {
        this.averageDenseSize = averageDenseSize;
    }

    /**
     * 返回稠密切片的平均填充率。
     *
     * @return 稠密切片平均填充率
     */
    public Double getAverageDenseFill() {
        return averageDenseFill;
    }

    public void setAverageDenseFill(Double averageDenseFill) {
        this.averageDenseFill = averageDenseFill;
    }

    /**
     * 返回稀疏切片的平均大小。
     *
     * @return 稀疏切片平均大小
     */
    public Double getAverageSparseSize() {
        return averageSparseSize;
    }

    public void setAverageSparseSize(Double averageSparseSize) {
        this.averageSparseSize = averageSparseSize;
    }

    @Override
    public String toString() {
        return "ArrayFullInfo{" +
                "count=" + getCount() +
                ", length=" + getLength() +
                ", nextInsertIndex=" + getNextInsertIndex() +
                ", slices=" + getSlices() +
                ", directorySize=" + getDirectorySize() +
                ", superDirectoryEntries=" + getSuperDirectoryEntries() +
                ", sliceSize=" + getSliceSize() +
                ", denseSlices=" + denseSlices +
                ", sparseSlices=" + sparseSlices +
                ", averageDenseSize=" + averageDenseSize +
                ", averageDenseFill=" + averageDenseFill +
                ", averageSparseSize=" + averageSparseSize +
                '}';
    }
}
