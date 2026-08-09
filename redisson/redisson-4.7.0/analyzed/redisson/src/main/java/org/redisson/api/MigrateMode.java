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
 * Redis 键迁移模式，对应 {@code MIGRATE} 命令的行为变体。
 *
 * @author lyrric
 */
public enum MigrateMode {

    /** 默认迁移：迁移后删除源节点上的键。 */

    MIGRATE,
    /** 复制模式：不删除源节点上的键。 */

    COPY,

    /** 替换模式：覆盖目标节点上已存在的同名键。 */

    REPLACE,

    /** 复制并替换：保留源键且覆盖目标节点上的同名键。 */

    COPY_AND_REPLACE;

}
