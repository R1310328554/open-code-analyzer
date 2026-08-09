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
package org.redisson.api.geo;

/**
 * 地理空间距离度量单位，对应 Redis GEO 命令中的单位字符串。
 * <p>
 * 各枚举常量的 {@link #toString()} 返回 Redis 接受的缩写（如 {@code m}、{@code km}）。
 *
 * @author Nikita Koksharov
 */
public enum GeoUnit {

    /** 米（Redis 单位 {@code m}）。 */
    METERS {
        @Override
        public String toString() {
            return "m";
        }
    },
    
    /** 千米（Redis 单位 {@code km}）。 */
    KILOMETERS {
        @Override
        public String toString() {
            return "km";
        }
    },
    
    /** 英里（Redis 单位 {@code mi}）。 */
    MILES {
        @Override
        public String toString() {
            return "mi";
        }
    },
    
    /** 英尺（Redis 单位 {@code ft}）。 */
    FEET {
        @Override
        public String toString() {
            return "ft";
        }
    }
    
}
