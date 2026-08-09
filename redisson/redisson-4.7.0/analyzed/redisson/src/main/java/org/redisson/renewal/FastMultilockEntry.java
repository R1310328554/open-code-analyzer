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
package org.redisson.renewal;

import java.util.Collection;

/**
 * 快速联锁（MultiLock）续期条目：
 * 在 {@link LockEntry} 基础上额外记录 Redis Hash 中的
 * 多个锁字段名，供 {@link FastMultilockTask} 批量续期 Lua 使用。
 *
 * @author Nikita Koksharov
 *
 */
public class FastMultilockEntry extends LockEntry {

    /** 联锁涉及的 Hash 字段集合。 */
    private final Collection<String> fields;

    /** @param fields 需一并续期的锁字段 */
    public FastMultilockEntry(Collection<String> fields) {
        this.fields = fields;
    }

    /** @return 锁字段集合 */
    public Collection<String> getFields() {
        return fields;
    }

}
