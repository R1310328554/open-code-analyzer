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
 * 大于（GT）数值范围条件。
 * <p>
 * 对数值索引字段使用 ZSET {@code valueRange(score, false, +∞, false)} 查询。
 *
 * @author Nikita Koksharov
 *
 */
public class GTCondition implements Condition {

    /** 索引字段名。 */
    private final String name;
    /** 严格大于的阈值。 */
    private final Number value;
    
    /** @param name 字段名；@param value 阈值 */
    public GTCondition(String name, Number value) {
        super();
        this.name = name;
        this.value = value;
    }

    /** 返回字段名。 */
    public String getName() {
        return name;
    }
    /** 返回阈值。 */
    public Number getValue() {
        return value;
    }

}
