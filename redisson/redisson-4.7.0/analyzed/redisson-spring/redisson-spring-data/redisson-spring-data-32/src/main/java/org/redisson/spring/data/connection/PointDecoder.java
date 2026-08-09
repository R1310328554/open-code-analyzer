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

import java.util.List;

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.decoder.MultiDecoder;
import org.springframework.data.geo.Point;

/**
 * 将 Redis GEO {@code GEOPOS} 等命令的经纬度对解码为 Spring {@link Point}。
 * <p>坐标分量使用 {@link DoubleCodec} 解析；空响应返回 {@code null}。
 *
 * @author Nikita Koksharov
 *
 */
public class PointDecoder implements MultiDecoder<Point> {

    /** 经纬度分量均按 {@code double} 解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return DoubleCodec.INSTANCE.getValueDecoder();
    }
    
    /** 从 [经度, 纬度] 两元素列表构造 {@link Point}。 */
    @Override
    public Point decode(List<Object> parts, State state) {
        // 无坐标数据时返回 null。
        if (parts.isEmpty()) {
            return null;
        }

        Double longitude = (Double)parts.get(0);
        Double latitude = (Double)parts.get(1);
        return new Point(longitude, latitude);
    }

}
