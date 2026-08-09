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
package org.redisson.api.atomic;

/**
 * 扩展原子 long 递增操作的参数接口。
 *
 * @author lamnt2008
 *
 */
public interface LongIncrementArgs extends BaseIncrementArgs<LongIncrementArgs> {

    /**
     * 使用默认递增量 {@code 1}。
     *
     * @return 参数对象
     */
    static LongIncrementArgs defaults() {
        return new LongIncrementParams();
    }

    /**
     * 设置递增量。
     *
     * @param increment 递增量
     * @return 参数对象
     */
    static LongIncrementArgs by(long increment) {
        return new LongIncrementParams(increment);
    }

    /**
     * 设置递增结果的下界。
     *
     * @param value 下界值
     * @return 参数对象
     */
    LongIncrementArgs lowerBound(long value);

    /**
     * 设置递增结果的上界。
     *
     * @param value 上界值
     * @return 参数对象
     */
    LongIncrementArgs upperBound(long value);

}
