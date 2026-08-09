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

import org.redisson.codec.JsonCodec;

import java.util.Objects;

/**
 * {@link VectorAddArgs} 的实现类，承载向量集添加参数。
 *
 * @author Nikita Koksharov
 *
 */
public final class VectorAddParams implements VectorAddArgs, VectorAddArgs.ElementStep {
    /** 元素名称。 */
    private final String element;
    /** 字节数组形式的向量。 */
    private byte[] vectorBytes;
    /** 双精度数组形式的向量。 */
    private Double[] vectorDoubles;
    /** 随机投影降维值。 */
    private Integer reduce;
    /** 是否使用 check-and-set 模式。 */
    private boolean useCheckAndSet;
    /** 向量量化类型。 */
    private QuantizationType quantizationType;
    /** 探索因子（EF）。 */
    private Integer effort;
    /** 附加属性对象。 */
    private Object attributes;
    /** 节点最大连接数。 */
    private Integer maxConnections;
    /** 属性序列化编解码器。 */
    private JsonCodec attributesJsonCodec;

    VectorAddParams(String element) {
        this.element = element;
    }

    @Override
    public VectorAddArgs vector(byte[] vector) {
        this.vectorBytes = vector;
        return this;
    }

    @Override
    public VectorAddArgs vector(Double... vector) {
        this.vectorDoubles = vector;
        return this;
    }

    @Override
    public VectorAddArgs reduce(int reduce) {
        this.reduce = reduce;
        return this;
    }

    @Override
    public VectorAddArgs useCheckAndSet() {
        this.useCheckAndSet = true;
        return this;
    }

    @Override
    public VectorAddArgs quantization(QuantizationType type) {
        this.quantizationType = type;
        return this;
    }

    @Override
    public VectorAddArgs explorationFactor(int value) {
        this.effort = value;
        return this;
    }

    @Override
    public VectorAddArgs attributes(Object attributes, JsonCodec jsonCodec) {
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(jsonCodec);

        this.attributes = attributes;
        this.attributesJsonCodec = jsonCodec;
        return this;
    }

    @Override
    public VectorAddArgs maxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    public String getElement() {
        return element;
    }

    public byte[] getVectorBytes() {
        return vectorBytes;
    }

    public Double[] getVectorDoubles() {
        return vectorDoubles;
    }

    public Integer getReduce() {
        return reduce;
    }

    public boolean isUseCheckAndSet() {
        return useCheckAndSet;
    }

    public QuantizationType getQuantizationType() {
        return quantizationType;
    }

    public Integer getEffort() {
        return effort;
    }

    public Object getAttributes() {
        return attributes;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public JsonCodec getAttributesJsonCodec() {
        return attributesJsonCodec;
    }
}
