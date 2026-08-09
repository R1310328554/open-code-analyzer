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
package org.redisson.api.keys;


/**
 * 键迁移参数链中的「通信超时」配置步骤。
 * <p>
 * 在设置 database 之后调用，用于限制与目标实例通信的空闲时间。
 *
 * @author lyrric
 */
public interface TimeoutMigrateArgs {

    /**
     * 设置与目标实例通信过程中允许的最大空闲时间（毫秒）。
     * <p>
     * 超过该时间未收到响应则中断迁移操作。
     *
     * @param timeout 超时毫秒数
     * @return 迁移条件构建器，可继续设置可选参数
     */
    OptionalMigrateArgs timeout(long timeout);
}
