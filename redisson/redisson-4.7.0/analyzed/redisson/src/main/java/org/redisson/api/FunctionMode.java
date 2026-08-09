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
 * Redis 函数 {@code FCALL} 的执行模式，映射为只读或读写命令。
 *
 * @author Nikita Koksharov
 */
public enum FunctionMode {

    /** 以只读操作执行函数（{@code FCALL_RO}）。 */
    READ,

    /** 以写操作执行函数（{@code FCALL}）。 */
    WRITE

}
