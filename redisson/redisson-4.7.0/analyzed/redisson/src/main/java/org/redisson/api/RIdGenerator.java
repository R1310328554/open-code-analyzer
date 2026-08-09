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
 * Long 型分布式 ID 生成器。
 * <p>返回唯一 Long 型 ID，但不保证严格单调递增；本地预分配区间以减少 Redis 访问。
 *
 * @author Nikita Koksharov
 */
public interface RIdGenerator extends RExpirable, RIdGeneratorAsync {

    /**
     * 初始化 ID 生成器参数。
     *
     * @param value 初始值
     * @param allocationSize 预分配区间大小
     * @return 见方法说明
     *         <code>false</code> if Id generator already initialized
     */
    boolean tryInit(long value, long allocationSize);

    /**
     * 返回下一个唯一 ID（非严格单调递增）。
     *
     * @return 唯一元素近似基数
     */
    long nextId();

}
