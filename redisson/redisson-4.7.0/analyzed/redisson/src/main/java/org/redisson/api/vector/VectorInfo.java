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
package org.redisson.api.vector;

import java.io.Serializable;

/**
 * 向量信息对象。
 * <p>
 * 描述向量集合的维度、规模、量化类型及图连接数等元数据。
 *
 * @author Nikita Koksharov
 *
 */
public final class VectorInfo implements Serializable {

    /** 属性数量。 */
    private long attributesCount;
    /** 向量维度。 */
    private long dimensions;
    /** 向量集合大小（元素个数）。 */
    private long size;
    /** 量化类型。 */
    private QuantizationType quantizationType;
    /** 每个节点的最大连接数。 */
    private long maxConnections;

    public long getAttributesCount() {
        return attributesCount;
    }

    public void setAttributesCount(long attributesCount) {
        this.attributesCount = attributesCount;
    }

    public long getDimensions() {
        return dimensions;
    }

    public void setDimensions(long dimensions) {
        this.dimensions = dimensions;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public QuantizationType getQuantizationType() {
        return quantizationType;
    }

    public void setQuantizationType(QuantizationType quantizationType) {
        this.quantizationType = quantizationType;
    }

    public long getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(long maxConnections) {
        this.maxConnections = maxConnections;
    }
}
