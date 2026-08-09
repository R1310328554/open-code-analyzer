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

import java.util.concurrent.TimeUnit;

import org.redisson.client.protocol.convertor.Convertor;

/**
 * 将 Redis 时间数值在 {@link TimeUnit} 间转换的协议层转换器。
 * <p>Spring Data Redis 命令参数常以秒为单位，需与目标 {@code TimeUnit} 对齐。
 *
 * @author Nikita Koksharov
 *
 */
public class SecondsConvertor implements Convertor<Long> {

    /** 目标时间单位。 */
    private final TimeUnit unit;
    /** 源时间单位（Redis 返回值侧）。 */
    private final TimeUnit source;
    
    /** 指定源单位与目标单位的转换关系。 */
    public SecondsConvertor(TimeUnit unit, TimeUnit source) {
        super();
        this.unit = unit;
        this.source = source;
    }

    /** 将 {@code Long} 时长从 {@code source} 换算为 {@code unit}。 */
    @Override
    public Long convert(Object obj) {
        return unit.convert((Long)obj, source);
    }

}
