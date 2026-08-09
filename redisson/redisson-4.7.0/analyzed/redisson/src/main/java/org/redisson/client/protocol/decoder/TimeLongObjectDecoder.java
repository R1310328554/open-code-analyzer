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

import org.redisson.client.handler.State;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis 时间戳二元组解码器。
 * <p>
 * 将 {@code [秒, 微秒]} 两个 {@link Long} 元素合成为毫秒精度时间戳；
 * 可选 {@link TimeUnit} 在解码后进一步换算单位。
 *
 * @author Nikita Koksharov
 *
 */

public class TimeLongObjectDecoder implements MultiDecoder<Long> {

    /** 目标时间单位；{@code null} 表示直接返回毫秒值。 */
    private final TimeUnit timeUnit;

    /** 默认不解码后换算，输出毫秒时间戳。 */
    public TimeLongObjectDecoder() {
        this(null);
    }

    /** @param timeUnit 解码完成后要转换到的 {@link TimeUnit} */
    public TimeLongObjectDecoder(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /** 秒×1000 + 微秒÷1000 合成毫秒，再按需 {@link TimeUnit#convert}。 */
    @Override
    public Long decode(List<Object> parts, State state) {
        long time = ((Long) parts.get(0)) * 1000L + ((Long) parts.get(1)) / 1000L;
        if (timeUnit != null) {
            return timeUnit.convert(time, TimeUnit.MILLISECONDS);
        }
        return time;
    }
    
}
