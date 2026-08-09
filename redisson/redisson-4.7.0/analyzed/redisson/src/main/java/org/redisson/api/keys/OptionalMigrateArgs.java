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

import org.redisson.api.MigrateMode;


/**
 * 键迁移参数链中的可选配置步骤。
 * <p>
 * 在设置 timeout 之后，可进一步指定迁移模式与目标实例认证信息。
 *
 * @author lyrric
 */
public interface OptionalMigrateArgs extends MigrateArgs {


    /**
     * 设置键迁移模式（迁移或复制）。
     * @see org.redisson.api.MigrateMode
     *
     * @param mode 迁移模式
     * @return 迁移条件构建器
     */
    OptionalMigrateArgs mode(MigrateMode mode);

    /**
     * 设置目标 Redis 实例的 ACL 用户名。
     * <p>
     * 使用给定用户名对远程实例进行认证；若设置用户名则通常需同时设置密码。
     * <p>
     * 适用于 Redis 6+ 的 ACL 认证方式。
     *
     * @param username 目标实例用户名
     * @return 迁移条件构建器
     */
    OptionalMigrateArgs username(String username);

    /**
     * 设置目标 Redis 实例的访问密码。
     * <p>
     * 使用给定密码对远程实例进行认证。
     *
     * @param password 目标实例密码
     * @return 迁移条件构建器
     */
    OptionalMigrateArgs password(String password);

}
