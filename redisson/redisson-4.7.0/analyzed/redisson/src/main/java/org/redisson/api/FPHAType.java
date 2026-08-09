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
package org.redisson.api;

/**
 * {@code JSON.SET} 命令 {@code FPHA} 参数的浮点同质数组精度类型。
 * <p>
 * 需要 <b>Redis 8.8.0 或更高版本</b>。
 *
 * @author Triet Nguyen
 */
public enum FPHAType {
    /** Brain Float 16 位精度（BF16）。 */
    BF16,
    /** 16 位浮点精度（FP16）。 */
    FP16,
    /** 32 位浮点精度（FP32）。 */
    FP32,
    /** 64 位浮点精度（FP64）。 */
    FP64
}
