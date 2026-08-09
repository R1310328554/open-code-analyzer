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
 * 地理形状（GEOSHAPE）字段索引配置接口。
 *
 * @author seakider
 *
 */
public interface GeoShapeIndex extends FieldIndex {

    /** 坐标系类型：球面或平面。 */
    enum CoordinateSystems {SPHERICAL, FLAT}

    /**
     * 指定字段映射到的文档属性名。
     *
     * @param as 关联属性名
     * @return 当前地理形状索引选项
     */
    GeoShapeIndex as(String as);

    /**
     * 设置形状数据使用的坐标系。
     *
     * @param coordinateSystems 坐标系
     * @return 当前地理形状索引选项
     */
    GeoShapeIndex coordinateSystems(CoordinateSystems coordinateSystems);

    /**
     * 标记该属性不参与索引。
     *
     * @return 当前地理形状索引选项
     */
    GeoShapeIndex noIndex();

    /**
     * 索引缺少该属性的文档。
     *
     * @return 当前地理形状索引选项
     */
    GeoShapeIndex indexMissing();
}
