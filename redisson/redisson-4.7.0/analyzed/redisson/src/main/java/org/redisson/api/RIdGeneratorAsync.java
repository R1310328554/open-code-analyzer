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
 * {@code Long} 型分布式 ID 生成器异步 API。
 * <p>返回全局唯一 ID，但不保证严格单调递增。
 *
 * @author Nikita Koksharov
 */
public interface RIdGeneratorAsync extends RExpirableAsync {

    /**
     * 初始化 ID 生成器参数（起始值与预分配步长）。
     *
     * @param value 起始值
     * @param allocationSize 预分配步长
     * @return 见方法说明
     *         <code>false</code> if Id generator already initialized
     */
    RFuture<Boolean> tryInitAsync(long value, long allocationSize);

    /**
     * 返回下一个全局唯一 ID（不保证严格单调递增）。
     *
     * @return 下一个 ID
     */
    RFuture<Long> nextIdAsync();

}
