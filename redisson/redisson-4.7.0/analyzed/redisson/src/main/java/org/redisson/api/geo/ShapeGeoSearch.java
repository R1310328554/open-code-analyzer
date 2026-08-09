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
 * 地理搜索的形状定义阶段：在确定搜索中心后选择矩形或圆形区域。
 * <p>
 * 由 {@link GeoSearchArgs#from} 返回；调用 {@link #box} 或 {@link #radius} 后进入 {@link OptionalGeoSearch} 阶段。
 *
 * @author Nikita Koksharov
 */
public interface ShapeGeoSearch {

    /**
     * 在以搜索中心为基准的矩形区域内搜索。
     * <p>
     * 需要 <b>Redis 6.2.0 及以上版本。</b>
     *
     * @param width 矩形宽度
     * @param height 矩形高度
     * @param geoUnit 距离单位
     * @return 可选参数构建器
     */
    OptionalGeoSearch box(double width, double height, GeoUnit geoUnit);

    /**
     * 在指定半径的圆形区域内搜索。
     *
     * @param radius 半径（以 geoUnit 为单位）
     * @param geoUnit 距离单位
     * @return 可选参数构建器
     */
    OptionalGeoSearch radius(double radius, GeoUnit geoUnit);

}
