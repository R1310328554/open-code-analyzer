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
 * 等值（EQ）查询条件：指定字段等于给定值。
 * <p>
 * 数值字段走 ZSET 精确范围；其他类型走 SetMultimap 反向索引。
 *
 * @author Nikita Koksharov
 *
 */
public class EQCondition implements Condition {
    
    /** 索引字段名。 */
    private final String name;
    /** 期望的等值。 */
    private final Object value;
    
    /** @param name 字段名；@param value 比较值 */
    public EQCondition(String name, Object value) {
        super();
        this.name = name;
        this.value = value;
    }
    
    /** 返回字段名。 */
    public String getName() {
        return name;
    }
    
    /** 返回比较值。 */
    public Object getValue() {
        return value;
    }
    
}
