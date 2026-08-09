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
 * {@link org.redisson.api.RKeys#migrate(MigrateArgs)} 的键迁移参数入口。
 * <p>
 * 通过静态工厂 {@link #keys(String...)} 创建链式构建器，
 * 逐步配置目标实例连接信息与可选认证参数。
 *
 * @author lyrric
 */
public interface MigrateArgs {

    /**
     * 指定待迁移的键名列表。
     * <p>
     * 需要 Redis 3.0.6 及以上版本；键数组不可为空。
     *
     * @param keys 待迁移的键，不可为空
     * @return 迁移条件构建器，可继续设置 host
     */
    static HostMigrateArgs keys(String... keys){
        return new MigrateParams(keys);
    }
}
