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
package org.redisson.client.protocol.decoder;

import java.util.List;

import org.redisson.api.geo.GeoPosition;
import org.redisson.client.codec.Codec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 单个 Geo 坐标（经度/纬度）解码器。
 * <p>
 * 将 Redis {@code GEOPOS} 等返回的两元素数组解析为 {@link GeoPosition}；
 * 空数组表示该成员无坐标。
 *
 * @author Nikita Koksharov
 *
 */
public class GeoPositionDecoder implements MultiDecoder<GeoPosition> {

    /** 经纬度数值均用 {@link DoubleCodec} 解码。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return DoubleCodec.INSTANCE.getValueDecoder();
    }
    
    /** 空回复返回 {@code null}；否则取 [longitude, latitude]。 */
    @Override
    public GeoPosition decode(List<Object> parts, State state) {
        if (parts.isEmpty()) {
            return null;
        }

        Double longitude = (Double) parts.get(0);
        Double latitude = (Double) parts.get(1);
        return new GeoPosition(longitude, latitude);
    }

}
