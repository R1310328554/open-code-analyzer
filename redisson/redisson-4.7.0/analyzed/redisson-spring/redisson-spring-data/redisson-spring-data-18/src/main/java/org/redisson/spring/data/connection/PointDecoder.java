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
 * 将 Redis GEO 坐标对（经度、纬度）解码为 Spring {@link Point}。
 * <p>空列表返回 {@code null}；各坐标分量以 {@link DoubleCodec} 解析。
 *
 * @author Nikita Koksharov
 *
 */
public class PointDecoder implements MultiDecoder<Point> {

    /** 经纬度分量均按 double 解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return DoubleCodec.INSTANCE.getValueDecoder();
    }
    
    /** 从前两个 double 元素构造 {@link Point}(longitude, latitude)。 */
    @Override
    public Point decode(List<Object> parts, State state) {
        // Redis 返回空列表表示该 member 无坐标。
        if (parts.isEmpty()) {
            return null;
        }

        Double longitude = (Double)parts.get(0);
        Double latitude = (Double)parts.get(1);
        return new Point(longitude, latitude);
    }

}
