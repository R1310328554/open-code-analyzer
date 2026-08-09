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
 * {@link org.redisson.api.RGeo#search(GeoSearchArgs)} 等地理搜索方法的参数入口。
 * <p>
 * 通过静态工厂 {@link #from(Object)} 或 {@link #from(double, double)} 指定搜索中心，
 * 再链式调用 {@link ShapeGeoSearch}、{@link OptionalGeoSearch} 方法完善条件。
 * <p>
 * 同步/异步/Reactive 变体均接受本类型：
 * {@link org.redisson.api.RGeo#search(GeoSearchArgs)}、
 * {@link org.redisson.api.RGeoAsync#searchAsync(GeoSearchArgs)}、
 * {@link org.redisson.api.RGeoRx#search(GeoSearchArgs)}、
 * {@link org.redisson.api.RGeoReactive#search(GeoSearchArgs)}
 *
 * @author Nikita Koksharov
 */
public interface GeoSearchArgs {

    /**
     * 以集合中已有成员的位置作为搜索中心。
     *
     * @param member 成员对象
     * @return 形状搜索条件构建器
     */
    static <V> ShapeGeoSearch from(V member) {
        return new GeoSearchParams(member);
    }

    /**
     * 以给定经纬度坐标作为搜索中心。
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @return 形状搜索条件构建器
     */
    static ShapeGeoSearch from(double longitude, double latitude) {
        return new GeoSearchParams(longitude, latitude);
    }

}
