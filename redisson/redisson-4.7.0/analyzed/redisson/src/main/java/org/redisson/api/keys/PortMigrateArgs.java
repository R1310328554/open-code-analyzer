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
 * 键迁移参数链中的「目标端口」配置步骤。
 * <p>
 * 在设置 host 之后调用，用于指定远程 Redis 监听端口。
 *
 * @author lyrric
 */
public interface PortMigrateArgs {

    /**
     * 设置目标 Redis 实例的 TCP 端口。
     *
     * @param port 目标端口
     * @return 迁移条件构建器，可继续设置数据库
     */
    DatabaseMigrateArgs port(int port);
}
