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
package org.redisson.api.search.index;

/**
 * {@link FlatVectorIndex} 的参数实现，串联类型、维度、距离度量与可选 FLAT 参数。
 *
 * @author Nikita Koksharov
 *
 */
public final class FlatVectorIndexParams implements FlatVectorIndex,
                                              VectorDimParam<FlatVectorOptionalArgs>,
                                              VectorDistParam<FlatVectorOptionalArgs>,
                                              FlatVectorOptionalArgs {

    private final String fieldName;
    private VectorTypeParam.Type type;
    private int dim;
    private VectorDistParam.DistanceMetric distanceMetric;
    private Integer initialCapacity;
    private Integer blockSize;
    private int count;
    private String as;

    FlatVectorIndexParams(String name) {
        this.fieldName = name;
    }

    @Override
    public FlatVectorIndexParams as(String as) {
        this.as = as;
        return this;
    }

    @Override
    public VectorDimParam<FlatVectorOptionalArgs> type(Type type) {
        count++;
        this.type = type;
        return this;
    }

    @Override
    public VectorDistParam<FlatVectorOptionalArgs> dim(int value) {
        count++;
        this.dim = value;
        return this;
    }

    @Override
    public FlatVectorOptionalArgs distance(DistanceMetric metric) {
        count++;
        this.distanceMetric = metric;
        return this;
    }

    @Override
    public FlatVectorOptionalArgs initialCapacity(int value) {
        count++;
        this.initialCapacity = value;
        return this;
    }

    @Override
    public FlatVectorOptionalArgs blockSize(int value) {
        count++;
        this.blockSize = value;
        return this;
    }

    /** 返回索引字段名。 */
    public String getFieldName() {
        return fieldName;
    }

    /** 返回向量元素类型。 */
    public Type getType() {
        return type;
    }

    /** 返回向量维度。 */
    public int getDim() {
        return dim;
    }

    /** 返回距离度量方式。 */
    public DistanceMetric getDistanceMetric() {
        return distanceMetric;
    }

    /** 返回初始容量，未设置时为 {@code null}。 */
    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    /** 返回块大小，未设置时为 {@code null}。 */
    public Integer getBlockSize() {
        return blockSize;
    }

    /** 返回已配置的向量参数项数量。 */
    public int getCount() {
        return count;
    }

    /** 返回映射的属性别名。 */
    public String getAs() {
        return as;
    }
}
