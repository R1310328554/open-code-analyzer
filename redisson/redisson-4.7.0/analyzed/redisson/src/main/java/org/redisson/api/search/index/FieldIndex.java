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
 * 搜索索引字段定义工厂，供 {@link org.redisson.api.RSearch#createIndex(String, IndexOptions, FieldIndex...)} 使用。
 * <p>
 * 提供数值、标签、文本、向量及地理等字段类型的静态构造入口。
 *
 * @author Nikita Koksharov
 *
 */
public interface FieldIndex {

    /**
     * 创建数值字段索引。
     *
     * @param fieldName 字段名
     * @return 数值索引选项
     */
    static NumericIndex numeric(String fieldName) {
        return new NumericIndexParams(fieldName);
    }

    /**
     * 创建标签（TAG）字段索引。
     *
     * @param fieldName 字段名
     * @return 标签索引选项
     */
    static TagIndex tag(String fieldName) {
        return new TagIndexParams(fieldName);
    }

    /**
     * 创建全文（TEXT）字段索引。
     *
     * @param fieldName 字段名
     * @return 文本索引选项
     */
    static TextIndex text(String fieldName) {
        return new TextIndexParams(fieldName);
    }

    /**
     * 创建采用 FLAT 算法的向量字段索引。
     *
     * @param fieldName 字段名
     * @return FLAT 向量索引选项
     */
    static FlatVectorIndex flatVector(String fieldName) {
        return new FlatVectorIndexParams(fieldName);
    }

    /**
     * 创建采用 HNSW 算法的向量字段索引。
     *
     * @param fieldName 字段名
     * @return HNSW 向量索引选项
     */
    static HNSWVectorIndex hnswVector(String fieldName) {
        return new HNSWVectorIndexParams(fieldName);
    }

    /**
     * 创建采用 SVS-VAMANA 算法的向量字段索引。
     *
     * @param fieldName 字段名
     * @return SVS-VAMANA 向量索引选项
     */
    static SVSVamanaVectorIndex svsVamanaVector(String fieldName) {
        return new SVSVamanaVectorIndexParams(fieldName);
    }

    /**
     * 创建地理坐标（GEO）字段索引。
     *
     * @param fieldName 字段名
     * @return 地理索引选项
     */
    static GeoIndex geo(String fieldName) {
        return new GeoIndexParams(fieldName);
    }

    /**
     * 创建地理形状（GEOSHAPE）字段索引。
     *
     * @param fieldName 字段名
     * @return 地理形状索引选项
     */
    static GeoShapeIndex geoShape(String fieldName) {
        return new GeoShapeIndexParams(fieldName);
    }
}
