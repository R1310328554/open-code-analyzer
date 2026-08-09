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
 * 地理空间集合中的单条成员及其经纬度坐标。
 * <p>
 * 用于 {@link org.redisson.api.RGeo#add(GeoEntry...)} 批量写入 Redis GEO 数据结构。
 *
 * @author Nikita Koksharov
 */
public class GeoEntry {

    /** 经度（longitude）。 */
    private final double longitude;
    /** 纬度（latitude）。 */
    private final double latitude;
    /** 集合成员标识，可为任意对象。 */
    private final Object member;
    
    /**
     * 创建地理条目。
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @param member 成员标识
     */
    public GeoEntry(double longitude, double latitude, Object member) {
        super();
        this.longitude = longitude;
        this.latitude = latitude;
        this.member = member;
    }
    
    /** 返回纬度。 */
    public double getLatitude() {
        return latitude;
    }
    
    /** 返回经度。 */
    public double getLongitude() {
        return longitude;
    }
    
    /** 返回成员标识。 */
    public Object getMember() {
        return member;
    }
    
}
