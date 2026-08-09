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

import org.redisson.client.codec.Codec;
import org.redisson.client.codec.DoubleCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

/**
 * 有序集合扫描（ZSCAN）游标结果解码器。
 * <p>
 * 继承 {@link ObjectListReplayDecoder} 的列表回放能力，
 * 针对 WITHSCORES 响应在奇数索引处注入 {@link DoubleCodec} 解码 score。
 *
 * @author Nikita Koksharov
 *
 * @param <T> type
 */
public class ScoredSortedSetScanDecoder<T> extends ObjectListReplayDecoder<T> {

    /** 奇数索引为 score（Double），偶数索引沿用父类 member 解码策略。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        if (paramNum % 2 != 0) {
            return DoubleCodec.INSTANCE.getValueDecoder();
        }
        return super.getDecoder(codec, paramNum, state, size);
    }
    
}
