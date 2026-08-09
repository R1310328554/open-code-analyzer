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

import java.util.Objects;

/**
 * 不可变的经纬度坐标对，表示地理空间中的一个点。
 * <p>
 * 实现 {@link #equals} 与 {@link #hashCode}，可在集合中作为键或值使用。
 *
 * @author Nikita Koksharov
 */
public class GeoPosition {

    /** 经度。 */
    private final double longitude;
    /** 纬度。 */
    private final double latitude;
    
    /**
     * 以给定经纬度创建坐标。
     *
     * @param longitude 经度
     * @param latitude 纬度
     */
    public GeoPosition(double longitude, double latitude) {
        super();
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /** 返回纬度。 */
    public double getLatitude() {
        return latitude;
    }
    
    /** 返回经度。 */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoPosition that = (GeoPosition) o;
        return Double.compare(longitude, that.longitude) == 0 && Double.compare(latitude, that.latitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }

    @Override
    public String toString() {
        return "GeoPosition [longitude=" + longitude + ", latitude=" + latitude + "]";
    }

}
