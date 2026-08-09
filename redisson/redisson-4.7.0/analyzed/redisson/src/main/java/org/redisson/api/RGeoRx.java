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
package org.redisson.api;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.redisson.api.geo.GeoEntry;
import org.redisson.api.geo.GeoPosition;
import org.redisson.api.geo.GeoSearchArgs;
import org.redisson.api.geo.GeoUnit;

import java.util.List;
import java.util.Map;

/**
 * {@link RGeo} RxJava3 风格 API。
 * <p>各方法返回 {@link Single} 或 {@link Maybe}。
 *
 * @author Nikita Koksharov
 * @param <V> 成员类型
 */
public interface RGeoRx<V> extends RScoredSortedSetRx<V> {

    /**
     * 添加带经纬度的地理空间成员。
     * 
     * @param longitude 经度
     * @param latitude 纬度
     * @param member 成员对象
     * @return 新增元素数量
     * 不含已存在且仅更新分数的元素；
     * 已存在成员的分数被更新时不计入新增数。
     */
    Single<Long> add(double longitude, double latitude, V member);

    /**
     * 批量添加地理空间成员。
     * 
     * @param entries 地理空间条目
     * @return 新增元素数量
     * 不含已存在且仅更新分数的元素；
     * 已存在成员的分数被更新时不计入新增数。
     */
    Single<Long> add(GeoEntry... entries);

    /**
     * 仅当成员已存在时更新坐标（GEOADD XX）。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @param member 成员对象
     * @return 新增元素数量
     */
    Single<Long> addIfExists(double longitude, double latitude, V member);

    /**
     * 批量：仅当成员已存在时更新坐标。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param entries 地理空间条目
     * @return 新增元素数量
     */
    Single<Long> addIfExists(GeoEntry... entries);

    /**
     * 仅当成员不存在时添加（GEOADD NX）。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @param member 成员对象
     * @return 新增元素数量
     */
    Single<Boolean> tryAdd(double longitude, double latitude, V member);

    /**
     * 批量：仅当成员不存在时添加。
     * <p>
     * Requires <b>Redis 6.2.0 and higher.</b>
     *
     * @param entries 地理空间条目
     * @return 新增元素数量
     */
    Single<Long> tryAdd(GeoEntry... entries);

    /**
     * 返回两成员间距离（指定 {@link GeoUnit} 单位）。
     * 
     * @param firstMember 第一个成员
     * @param secondMember 第二个成员
     * @param geoUnit 距离单位
     * @return 距离
     */
    Single<Double> dist(V firstMember, V secondMember, GeoUnit geoUnit);
    
    /**
     * 返回成员对应的 11 位 Geohash 字符串映射。
     * 
     * @param members 成员集合
     * @return 成员到 Geohash 的映射
     */
    Maybe<Map<V, String>> hash(V... members);

    /**
     * 返回成员经纬度坐标映射。
     * 
     * @param members 成员集合
     * @return 成员到坐标的映射
     */
    Maybe<Map<V, GeoPosition>> pos(V... members);

    /**
     * 按搜索条件返回范围内的成员列表。
     * borders of specified search conditions.
     * <p>
     * 用法示例：
     * <pre>
     * List objects = geo.search(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)
     *                                 .order(GeoOrder.ASC)
     *                                 .count(1)));
     * </pre>
     * <pre>
     * List objects = geo.search(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)));
     * </pre>
     * <p>
     * Requires <b>Redis 3.2.10 and higher.</b>
     *
     * @param args 搜索条件
     * @return 成员列表
     */
    Maybe<List<V>> search(GeoSearchArgs args);

    /**
     * 按搜索条件返回成员及距离的映射。
     * 在指定搜索条件范围内查找成员。
     * <p>
     * 用法示例：
     * <pre>
     * Map objects = geo.searchWithDistance(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)
     *                                 .order(GeoOrder.ASC)
     *                                 .count(1)));
     * </pre>
     * <pre>
     * Map objects = geo.searchWithDistance(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)));
     * </pre>
     * <p>
     * Requires <b>Redis 3.2.10 and higher.</b>
     *
     * @param args 搜索条件
     * @return 成员到距离的映射
     */
    Maybe<Map<V, Double>> searchWithDistance(GeoSearchArgs args);

    /**
     * 按搜索条件返回成员及坐标的映射。
     * 在指定搜索条件范围内查找成员。
     * <p>
     * 用法示例：
     * <pre>
     * Map objects = geo.searchWithPosition(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)
     *                                 .order(GeoOrder.ASC)
     *                                 .count(1)));
     * </pre>
     * <pre>
     * Map objects = geo.searchWithPosition(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)));
     * </pre>
     * <p>
     * Requires <b>Redis 3.2.10 and higher.</b>
     *
     * @param args 搜索条件
     * @return 成员到坐标的映射
     */
    Maybe<Map<V, GeoPosition>> searchWithPosition(GeoSearchArgs args);

    /**
     * 按搜索条件查找成员并写入目标键。
     * 在指定搜索条件范围内查找成员。
     * <p>
     * 将结果写入 {@code destName} 目标键。
     * <p>
     * 用法示例：
     * <pre>
     * long count = geo.storeSearchTo(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)
     *                                 .order(GeoOrder.ASC)
     *                                 .count(1)));
     * </pre>
     * <pre>
     * long count = geo.storeSearchTo(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)));
     * </pre>
     *
     * @param args 搜索条件
     * @return 结果集长度
     */
    Single<Long> storeSearchTo(String destName, GeoSearchArgs args);

    /**
     * 按搜索条件查找成员并写入目标键。
     * 在指定搜索条件范围内查找成员。
     * <p>
     * 将结果按距离排序后写入 {@code destName} 目标键。
     * <p>
     * 用法示例：
     * <pre>
     * long count = geo.storeSortedSearchTo(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)
     *                                 .order(GeoOrder.ASC)
     *                                 .count(1)));
     * </pre>
     * <pre>
     * long count = geo.storeSortedSearchTo(GeoSearchArgs.from(15, 37)
     *                                 .radius(200, GeoUnit.KILOMETERS)));
     * </pre>
     *
     * @param args 搜索条件
     * @return 结果集长度
     */
    RFuture<Long> storeSortedSearchTo(String destName, GeoSearchArgs args);

}
