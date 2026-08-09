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
package org.redisson.api.bitset;

/**
 * BITFIELD 命令的溢出处理方式。
 *
 * @author Su Ko
 *
 */
public enum BitFieldOverflow {
    /**
     * 溢出/下溢时回绕；无符号使用模运算，有符号在取值范围内回绕（默认）。
     */
    WRAP,

    /**
     * 溢出/下溢时饱和到最小/最大值；
     * 钳制到最近边界而非回绕。
     */
    SAT,

    /**
     * 溢出/下溢时返回 null；
     * 检测到溢出/下溢时不修改原值。
     */
    FAIL
}
