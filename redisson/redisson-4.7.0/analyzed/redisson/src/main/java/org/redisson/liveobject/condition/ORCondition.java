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
package org.redisson.liveobject.condition;

import org.redisson.api.condition.Condition;

/**
 * 逻辑或（OR）组合条件，任一子条件满足即匹配。
 * <p>
 * 由 {@link org.redisson.api.condition.Conditions#or} 构建，
 * 供 {@link LiveObjectSearch#find} 递归求并集。
 *
 * @author Nikita Koksharov
 *
 */
public class ORCondition implements Condition {
    
    /** 子条件数组，不可变。 */
    private final Condition[] conditions;

    /** @param conditions 满足其一即可的子条件列表 */
    public ORCondition(Condition[] conditions) {
        this.conditions = conditions;
    }

    /** 返回子条件数组。 */
    public Condition[] getConditions() {
        return conditions;
    }
    
}
