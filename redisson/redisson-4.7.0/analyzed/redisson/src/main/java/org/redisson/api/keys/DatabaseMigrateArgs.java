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
 * 键迁移参数链中的「目标数据库」配置步骤。
 * <p>
 * 在 {@link MigrateArgs#keys(String...)} 指定待迁移键后，
 * 依次设置 host、port、database 与 timeout 等参数。
 *
 * @author lyrric
 */
public interface DatabaseMigrateArgs {

    /**
     * 指定目标 Redis 实例的逻辑数据库编号。
     * <p>
     * 数据库索引应大于等于 0。
     *
     * @param database 目标数据库编号，应 ≥ 0
     * @return 迁移条件构建器，可继续设置超时等参数
     */
    TimeoutMigrateArgs database(int database);
}
