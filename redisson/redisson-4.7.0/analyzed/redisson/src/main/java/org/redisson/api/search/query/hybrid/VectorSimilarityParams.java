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
package org.redisson.api.search.query.hybrid;

/**
 * {@link VectorSimilarity} 的可变配置实现。
 * <p>
 * 同时实现范围检索、KNN 及基础配置接口，供混合查询构建器内部使用。
 *
 * @author Nikita Koksharov
 */
public final class VectorSimilarityParams implements VectorSimilarityRange, VectorSimilarityNearestNeighbors, VectorSimilarityBasic {

    /** 向量搜索模式：KNN 或范围检索。 */
    public enum VectorSearchMode {
        KNN,
        RANGE
    }

    /** 向量字段名。 */
    private final String field;
    /** 向量参数占位符名。 */
    private final String param;
    /** 当前选择的搜索模式。 */
    private VectorSearchMode mode;
    
    /** KNN 近邻数量。 */
    private Integer knnK;
    /** HNSW EF_RUNTIME 参数。 */
    private Integer efRuntime;
    /** 距离字段别名。 */
    private String yieldDistanceAs;
    /** 分片 K 比例。 */
    private Double shardKRatio;

    /** 范围检索半径。 */
    private Double rangeRadius;
    /** 范围检索 epsilon 精度。 */
    private Double rangeEpsilon;
    
    /** 相似度分数别名。 */
    private String scoreAlias;
    /** 预过滤表达式。 */
    private String filter;

    VectorSimilarityParams(String field, String param) {
        this.field = field;
        this.param = param;
        this.mode = null;
    }

    @Override
    public VectorSimilarityRange range(double radius) {
        this.mode = VectorSearchMode.RANGE;
        this.rangeRadius = radius;
        return this;
    }

    @Override
    public VectorSimilarityNearestNeighbors nearestNeighbors(int k) {
        this.mode = VectorSearchMode.KNN;
        this.knnK = k;
        return this;
    }

    @Override
    public VectorSimilarity epsilon(double epsilon) {
        this.rangeEpsilon = epsilon;
        return this;
    }

    @Override
    public VectorSimilarity efRuntime(int efRuntime) {
        this.efRuntime = efRuntime;
        return this;
    }

    @Override
    public VectorSimilarity yieldDistanceAs(String field) {
        this.yieldDistanceAs = field;
        return this;
    }

    @Override
    public VectorSimilarity shardKRatio(double ratio) {
        this.shardKRatio = ratio;
        return this;
    }

    @Override
    public VectorSimilarityBasic scoreAlias(String value) {
        this.scoreAlias = value;
        return this;
    }

    @Override
    public VectorSimilarityBasic filter(String value) {
        this.filter = value;
        return this;
    }

    /** 返回向量字段名。 */
    public String getField() {
        return field;
    }

    /** 返回向量参数占位符名。 */
    public String getParam() {
        return param;
    }

    /** 返回当前向量搜索模式。 */
    public VectorSearchMode getMode() {
        return mode;
    }

    /** 返回 KNN 近邻数量。 */
    public Integer getKnnK() {
        return knnK;
    }

    /** 返回 EF_RUNTIME 参数值。 */
    public Integer getEfRuntime() {
        return efRuntime;
    }

    /** 返回距离字段别名。 */
    public String getYieldDistanceAs() {
        return yieldDistanceAs;
    }

    /** 返回分片 K 比例。 */
    public Double getShardKRatio() {
        return shardKRatio;
    }

    /** 返回范围检索半径。 */
    public Double getRangeRadius() {
        return rangeRadius;
    }

    /** 返回范围检索 epsilon 值。 */
    public Double getRangeEpsilon() {
        return rangeEpsilon;
    }

    /** 返回相似度分数别名。 */
    public String getScoreAlias() {
        return scoreAlias;
    }

    /** 返回预过滤表达式。 */
    public String getFilter() {
        return filter;
    }
}
