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
 * {@link HNSWVectorIndex} 的参数实现，串联类型、维度、距离度量与可选 HNSW 参数。
 *
 * @author Nikita Koksharov
 *
 */
public final class HNSWVectorIndexParams implements HNSWVectorIndex,
                                            VectorDimParam<HNSWVectorOptionalArgs>,
                                            VectorDistParam<HNSWVectorOptionalArgs>,
        HNSWVectorOptionalArgs {

    private final String fieldName;
    private Type type;
    private int dim;
    private DistanceMetric distanceMetric;
    private Integer initialCap;
    private Integer m;
    private Integer efConstruction;
    private Integer efRuntime;
    private Double epsilon;
    private int count;
    private String as;

    HNSWVectorIndexParams(String name) {
        this.fieldName = name;
    }

    @Override
    public HNSWVectorIndexParams as(String as) {
        this.as = as;
        return this;
    }

    @Override
    public VectorDimParam<HNSWVectorOptionalArgs> type(Type type) {
        count++;
        this.type = type;
        return this;
    }

    @Override
    public VectorDistParam<HNSWVectorOptionalArgs> dim(int value) {
        count++;
        this.dim = value;
        return this;
    }

    @Override
    public HNSWVectorOptionalArgs distance(DistanceMetric metric) {
        count++;
        this.distanceMetric = metric;
        return this;
    }

    @Override
    public HNSWVectorOptionalArgs initialCapacity(int value) {
        count++;
        this.initialCap = value;
        return this;
    }

    @Override
    public HNSWVectorOptionalArgs m(int value) {
        count++;
        this.m = value;
        return this;
    }

    @Override
    public HNSWVectorOptionalArgs efConstruction(int value) {
        count++;
        this.efConstruction = value;
        return this;
    }

    @Override
    public HNSWVectorOptionalArgs efRuntime(int value) {
        count++;
        this.efRuntime = value;
        return this;
    }

    @Override
    public HNSWVectorOptionalArgs epsilon(double value) {
        count++;
        this.epsilon = value;
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
    public Integer getInitialCap() {
        return initialCap;
    }

    /** 返回 HNSW 最大出边数 M，未设置时为 {@code null}。 */
    public Integer getM() {
        return m;
    }

    /** 返回建图候选出边上限 efConstruction，未设置时为 {@code null}。 */
    public Integer getEfConstruction() {
        return efConstruction;
    }

    /** 返回搜索候选集上限 efRuntime，未设置时为 {@code null}。 */
    public Integer getEfRuntime() {
        return efRuntime;
    }

    /** 返回范围查询边界因子 epsilon，未设置时为 {@code null}。 */
    public Double getEpsilon() {
        return epsilon;
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
