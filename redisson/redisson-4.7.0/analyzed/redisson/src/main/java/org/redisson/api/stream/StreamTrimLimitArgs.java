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
 * 流裁剪命令的驱逐数量限制参数接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamTrimLimitArgs<T> {

    /**
     * 不限制单次裁剪可驱逐的条目数量。
     *
     * @return 参数对象
     */
    T noLimit();

    /**
     * 设置单次裁剪最多驱逐的条目数量。
     *
     * @param size 最大驱逐条目数
     * @return 参数对象
     */
    T limit(int size);

}
