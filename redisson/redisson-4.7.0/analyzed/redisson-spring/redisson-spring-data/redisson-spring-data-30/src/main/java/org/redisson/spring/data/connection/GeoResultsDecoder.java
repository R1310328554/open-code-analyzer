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
package org.redisson.spring.data.connection;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.decoder.MultiDecoder;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis GEO 批量响应解码器，产出 {@link GeoResults}{@code <}{@link GeoLocation}{@code <byte[]>}{@code >}。
 * <p>支持带距离（{@code metric} 非空）或带坐标（{@code Point}）两种嵌套列表格式；
单元素条目视为仅 member 无附加信息。
 *
 * @author Nikita Koksharov
 *
 */
public class GeoResultsDecoder implements MultiDecoder<GeoResults<GeoLocation<byte[]>>> {

    /** 距离度量；非空时嵌套列表第二项解析为 {@link Distance}。 */
    private final Metric metric;
    
    /** 默认构造：嵌套列表第二项解析为 {@link Point}。 */
    public GeoResultsDecoder() {
        this(null);
    }
    
    /** 指定距离度量；非空时第二项解析为带 {@code metric} 的 {@link Distance}。 */
    public GeoResultsDecoder(Metric metric) {
        super();
        this.metric = metric;
    }

    /** 遍历响应片段，组装 {@link GeoResult} 列表并包装为 {@link GeoResults}。 */
    @Override
    public GeoResults<GeoLocation<byte[]>> decode(List<Object> parts, State state) {
        List<GeoResult<GeoLocation<byte[]>>> result = new ArrayList<GeoResult<GeoLocation<byte[]>>>();
        for (Object object : parts) {
            if (object instanceof List) {
                List<Object> vals = ((List<Object>) object);
                
                // GEORADIUS 等带距离字段的响应。
                if (metric != null) {
                    GeoLocation<byte[]> location = new GeoLocation<byte[]>((byte[])vals.get(0), null);
                    result.add(new GeoResult<GeoLocation<byte[]>>(location, new Distance((Double)vals.get(1), metric)));
                // GEOPOS 等返回经纬度 Point 的响应。
                // 单字节数组 member，无距离/坐标附加项。
            } else {
                    GeoLocation<byte[]> location = new GeoLocation<byte[]>((byte[])vals.get(0), (Point)vals.get(1));
                    result.add(new GeoResult<GeoLocation<byte[]>>(location, new Distance(0)));
                }
            } else {
                GeoLocation<byte[]> location = new GeoLocation<byte[]>((byte[])object, null);
                result.add(new GeoResult<GeoLocation<byte[]>>(location, new Distance(0)));
            }
        }
        return new GeoResults<GeoLocation<byte[]>>(result);
    }

}
