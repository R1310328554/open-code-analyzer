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
package org.redisson.api.search.query;

/**
 * 用于 {@link QueryOptions#filters(QueryFilter...)} 的地理坐标过滤器。
 *
 * @author Nikita Koksharov
 *
 */
public interface GeoFilter {

    /**
     * 以给定经纬度坐标作为搜索圆心。
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @return 半径配置阶段
     */
    GeoFilterRadius from(double longitude, double latitude);

}
