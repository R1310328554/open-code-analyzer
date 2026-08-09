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
import org.redisson.api.bloomfilter.BloomFilterScanDumpInfo;
import org.redisson.client.codec.ByteArrayCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 布隆过滤器 {@code BF.SCANDUMP} 扫描导出解码器。
 * <p>
 * 解析迭代游标与二进制 dump 片段，封装为 {@link BloomFilterScanDumpInfo}。
 *
 * @author Su Ko
 *
 */
public class BloomFilterScanDumpInfoDecoder implements MultiDecoder<BloomFilterScanDumpInfo> {

    /** dump 数据段使用 {@link ByteArrayCodec} 解码为字节数组。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return ByteArrayCodec.INSTANCE.getValueDecoder();
    }

    /** 首项为下次扫描游标，次项为当前批次的二进制数据。 */
    @Override
    public BloomFilterScanDumpInfo decode(List<Object> parts, State state) {
        long iterator = (long) parts.get(0);
        byte[] data = (byte[]) parts.get(1);

        return new BloomFilterScanDumpInfo(iterator, data);
    }
}
