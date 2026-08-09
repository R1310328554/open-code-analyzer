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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis GEO 批量响应解码器，产出 {@link GeoResults}{@code <}{@link GeoLocation}{@code <ByteBuffer>}{@code >}。
 * <p>member 以 {@link ByteBuffer} 包装；支持带 {@link Distance}（指定 {@link Metric}）
或带 {@link Point} 坐标的嵌套列表；单元素条目仅含 member。
 *
 * @author Nikita Koksharov
 *
 */
public class ByteBufferGeoResultsDecoder implements MultiDecoder<GeoResults<GeoLocation<ByteBuffer>>> {

    /** 距离度量；非空时嵌套列表第二项解析为 {@link Distance}。 */
    private final Metric metric;
    
    /** 默认构造：嵌套列表第二项按 {@link Point} 解析坐标。 */
    public ByteBufferGeoResultsDecoder() {
        this(null);
    }
    
    /** 绑定 {@link Metric}，用于 GEORADIUS 等带距离字段的响应。 */
    public ByteBufferGeoResultsDecoder(Metric metric) {
        super();
        this.metric = metric;
    }

    /** 遍历响应片段，组装 {@link GeoResult} 列表并包装为 {@link GeoResults}。 */
    @Override
    public GeoResults<GeoLocation<ByteBuffer>> decode(List<Object> parts, State state) {
        List<GeoResult<GeoLocation<ByteBuffer>>> result = new ArrayList<GeoResult<GeoLocation<ByteBuffer>>>();
        for (Object object : parts) {
            if (object instanceof List) {
                List<Object> vals = ((List<Object>) object);
                
                // GEORADIUS BYMEMBER 等返回 member + 距离的响应。
                if (metric != null) {
                    GeoLocation<ByteBuffer> location = new GeoLocation<ByteBuffer>(ByteBuffer.wrap((byte[])vals.get(0)), null);
                    result.add(new GeoResult<GeoLocation<ByteBuffer>>(location, new Distance((Double)vals.get(1), metric)));
                // GEOPOS 等返回 member + 经纬度 Point 的响应。
                // 仅 member 字节数组，无距离或坐标附加项。
            } else {
                    GeoLocation<ByteBuffer> location = new GeoLocation<ByteBuffer>(ByteBuffer.wrap((byte[])vals.get(0)), (Point)vals.get(1));
                    result.add(new GeoResult<GeoLocation<ByteBuffer>>(location, null));
                }
            } else {
                GeoLocation<ByteBuffer> location = new GeoLocation<ByteBuffer>(ByteBuffer.wrap((byte[])object), null);
                result.add(new GeoResult<GeoLocation<ByteBuffer>>(location, new Distance(0)));
            }
        }
        return new GeoResults<GeoLocation<ByteBuffer>>(result);
    }

}
