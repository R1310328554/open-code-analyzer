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
 * {@link GeoSearchArgs}、{@link ShapeGeoSearch}、{@link OptionalGeoSearch} 的可变参数实现。
 * <p>
 * 封装搜索中心、形状（矩形/圆形）、结果数量限制及排序等条件，供 {@link org.redisson.api.RGeo} 内部使用。
 *
 * @author Nikita Koksharov
 */
public final class GeoSearchParams implements ShapeGeoSearch, OptionalGeoSearch {

    /** 以成员为搜索中心时的成员对象。 */
    private Object member;
    /** 以坐标为搜索中心时的经度。 */
    private Double longitude;
    /** 以坐标为搜索中心时的纬度。 */
    private Double latitude;
    /** 矩形搜索区域的宽度。 */
    private Double width;
    /** 矩形搜索区域的高度。 */
    private Double height;
    /** 圆形搜索半径。 */
    private Double radius;
    /** 距离单位。 */
    private GeoUnit unit;
    /** 返回结果数量上限。 */
    private Integer count;
    /** 为 true 时使用 ANY 语义（找到足够数量即返回，可能非最近）。 */
    private boolean countAny;
    /** 结果距离排序方式。 */
    private GeoOrder order;

    /** 以成员位置为搜索中心创建参数。 */
    GeoSearchParams(Object member) {
        this.member = member;
    }

    /** 以经纬度坐标为搜索中心创建参数。 */
    GeoSearchParams(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public OptionalGeoSearch box(double width, double height, GeoUnit geoUnit) {
        this.width = width;
        this.height = height;
        this.unit = geoUnit;
        return this;
    }

    @Override
    public OptionalGeoSearch radius(double radius, GeoUnit geoUnit) {
        this.radius = radius;
        this.unit = geoUnit;
        return this;
    }

    @Override
    public OptionalGeoSearch count(int value) {
        this.count = value;
        this.countAny = false;
        return this;
    }

    @Override
    public OptionalGeoSearch countAny(int value) {
        this.count = value;
        this.countAny = true;
        return this;
    }

    @Override
    public OptionalGeoSearch order(GeoOrder value) {
        this.order = value;
        return this;
    }

    /** 返回搜索中心成员。 */
    public Object getMember() {
        return member;
    }

    /** 返回搜索中心经度。 */
    public Double getLongitude() {
        return longitude;
    }

    /** 返回搜索中心纬度。 */
    public Double getLatitude() {
        return latitude;
    }

    /** 返回矩形宽度。 */
    public Double getWidth() {
        return width;
    }

    /** 返回矩形高度。 */
    public Double getHeight() {
        return height;
    }

    /** 返回圆形搜索半径。 */
    public Double getRadius() {
        return radius;
    }

    /** 返回距离单位。 */
    public GeoUnit getUnit() {
        return unit;
    }

    /** 返回结果数量上限。 */
    public Integer getCount() {
        return count;
    }

    /** 是否使用 ANY 计数语义。 */
    public boolean isCountAny() {
        return countAny;
    }

    /** 返回结果排序方式。 */
    public GeoOrder getOrder() {
        return order;
    }
}
