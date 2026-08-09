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
 * {@link SVSVamanaVectorIndex} 的参数实现，串联类型、维度、距离度量与可选 SVS-VAMANA 参数。
 *
 * @author seakider
 *
 */
public class SVSVamanaVectorIndexParams implements SVSVamanaVectorIndex,
                                            VectorDimParam<SVSVamanaVectorOptionalArgs>,
                                            VectorDistParam<SVSVamanaVectorOptionalArgs>,
                                            SVSVamanaVectorOptionalArgs {
    private final String fieldName;
    private Type type;
    private int dim;
    private DistanceMetric distanceMetric;
    private int count;
    private String as;

    private CompressionAlgorithm compressionAlgorithm;
    private Integer constructionWindowSize;
    private Integer graphMaxDegree;
    private Integer searchWindowSize;
    private Double epsilon;
    private Integer trainingThreshold;
    private Integer leanVecDim;

    SVSVamanaVectorIndexParams(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public SVSVamanaVectorIndexParams as(String as) {
        this.as = as;
        return this;
    }

    @Override
    public VectorDistParam<SVSVamanaVectorOptionalArgs> dim(int value) {
        count++;
        this.dim = value;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs distance(DistanceMetric metric) {
        count++;
        this.distanceMetric = metric;
        return this;
    }

    @Override
    public VectorDimParam<SVSVamanaVectorOptionalArgs> type(Type type) {
        count++;
        this.type = type;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs compression(CompressionAlgorithm algorithm) {
        count++;
        this.compressionAlgorithm = algorithm;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs constructionWindowSize(int value) {
        count++;
        this.constructionWindowSize = value;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs graphMaxDegree(int value) {
        count++;
        this.graphMaxDegree = value;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs searchWindowSize(int value) {
        count++;
        this.searchWindowSize = value;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs epsilon(double value) {
        count++;
        this.epsilon = value;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs trainingThreshold(int value) {
        count++;
        this.trainingThreshold = value;
        return this;
    }

    @Override
    public SVSVamanaVectorOptionalArgs leanVecDim(int value) {
        count++;
        this.leanVecDim = value;
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

    /** 返回已配置的向量参数项数量。 */
    public int getCount() {
        return count;
    }

    /** 返回映射的属性别名。 */
    public String getAs() {
        return as;
    }

    /** 返回压缩算法，未设置时为 {@code null}。 */
    public CompressionAlgorithm getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    /** 返回建图搜索窗口大小，未设置时为 {@code null}。 */
    public Integer getConstructionWindowSize() {
        return constructionWindowSize;
    }

    /** 返回图最大度数，未设置时为 {@code null}。 */
    public Integer getGraphMaxDegree() {
        return graphMaxDegree;
    }

    /** 返回查询搜索窗口大小，未设置时为 {@code null}。 */
    public Integer getSearchWindowSize() {
        return searchWindowSize;
    }

    /** 返回范围查询边界因子 epsilon，未设置时为 {@code null}。 */
    public Double getEpsilon() {
        return epsilon;
    }

    /** 返回压缩训练向量阈值，未设置时为 {@code null}。 */
    public Integer getTrainingThreshold() {
        return trainingThreshold;
    }

    /** 返回 LeanVec 压缩维度，未设置时为 {@code null}。 */
    public Integer getLeanVecDim() {
        return leanVecDim;
    }
}
