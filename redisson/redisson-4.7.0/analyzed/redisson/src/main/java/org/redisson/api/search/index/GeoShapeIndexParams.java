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
 * {@link GeoShapeIndex} 的参数实现。
 *
 * @author seakider
 *
 */
public class GeoShapeIndexParams implements GeoShapeIndex {

    private boolean noIndex;
    private String fieldName;
    private String as;
    private boolean indexMissing;
    private CoordinateSystems coordinateSystems;

    GeoShapeIndexParams(String name) {
        this.fieldName = name;
    }

    @Override
    public GeoShapeIndex as(String as) {
        this.as = as;
        return this;
    }

    @Override
    public GeoShapeIndex coordinateSystems(CoordinateSystems coordinateSystems) {
        this.coordinateSystems = coordinateSystems;
        return this;
    }

    @Override
    public GeoShapeIndex noIndex() {
        this.noIndex = true;
        return this;
    }

    @Override
    public GeoShapeIndex indexMissing() {
        this.indexMissing = true;
        return this;
    }

    /** 是否标记为不索引。 */
    public boolean isNoIndex() {
        return noIndex;
    }

    /** 返回字段名。 */
    public String getFieldName() {
        return fieldName;
    }

    /** 返回映射的属性别名。 */
    public String getAs() {
        return as;
    }

    /** 是否索引缺失该属性的文档。 */
    public boolean isIndexMissing() {
        return indexMissing;
    }

    /** 返回配置的坐标系。 */
    public CoordinateSystems getCoordinateSystems() {
        return coordinateSystems;
    }
}
