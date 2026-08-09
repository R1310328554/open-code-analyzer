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
package org.redisson.client.protocol.convertor;

/**
 * 将 Redis 整数回复与指定阈值比较，不相等时为 {@code true}。
 * <p>
 * 构造时传入期望的"失败"数值，用于检测命令是否返回了非预期状态码。
 *
 * @author Nikita Koksharov
 *
 */
public class BooleanNumberReplayConvertor implements Convertor<Boolean> {

    /** 与之比较的基准整数值。 */
    private final long number;

    /** @param number 期望比较的整型阈值 */
    public BooleanNumberReplayConvertor(long number) {
        super();
        this.number = number;
    }

    /** 回复整数不等于 {@link #number} 时返回 {@code true}。 */
    @Override
    public Boolean convert(Object obj) {
        return (Long) obj != number;
    }


}
