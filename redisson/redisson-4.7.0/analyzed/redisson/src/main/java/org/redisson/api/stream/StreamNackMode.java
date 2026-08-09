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
package org.redisson.api.stream;

/**
 * {@code RStream.nack()} 否定确认操作使用的模式枚举。
 *
 * @author lamnt2008
 *
 */
public enum StreamNackMode {

    /** 静默模式，不抛出异常。 */
    SILENT,

    /** 失败模式，操作失败时抛出异常。 */
    FAIL,

    /** 致命模式，遇到错误时立即终止。 */
    FATAL
}
