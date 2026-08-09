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
package org.redisson.api.geo;

/**
 * 地理搜索的可选参数阶段：在确定搜索形状后可设置结果数量与排序。
 * <p>
 * 由 {@link ShapeGeoSearch#box} 或 {@link ShapeGeoSearch#radius} 返回，
 * 链式调用完成后作为 {@link GeoSearchArgs} 传给 {@link org.redisson.api.RGeo#search}。
 *
 * @author Nikita Koksharov
 */
public interface OptionalGeoSearch extends GeoSearchArgs {

    /**
     * 限制返回结果数量（按距离排序后取前 N 条）。
     *
     * @param value 结果数量上限
     * @return 当前搜索条件对象
     */
    OptionalGeoSearch count(int value);

    /**
     * 限制返回结果数量，采用 ANY 语义：找到足够匹配即返回，可能非距离最近但更快。
     *
     * @param value 结果数量上限
     * @return 当前搜索条件对象
     */
    OptionalGeoSearch countAny(int value);

    /**
     * 指定结果按距离排序的方式。
     *
     * @param geoOrder 排序枚举（升序或降序）
     * @return 当前搜索条件对象
     */
    OptionalGeoSearch order(GeoOrder geoOrder);

}
