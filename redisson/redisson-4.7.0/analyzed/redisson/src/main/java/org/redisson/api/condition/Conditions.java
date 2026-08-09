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
package org.redisson.api.condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.redisson.liveobject.condition.ANDCondition;
import org.redisson.liveobject.condition.EQCondition;
import org.redisson.liveobject.condition.GECondition;
import org.redisson.liveobject.condition.GTCondition;
import org.redisson.liveobject.condition.LECondition;
import org.redisson.liveobject.condition.LTCondition;
import org.redisson.liveobject.condition.ORCondition;

/**
 * 按字段构建 Live Object 查询条件的工厂类。
 * <p>
 * 提供等于、范围、IN、AND/OR 等组合条件的静态方法。
 *
 * @author Nikita Koksharov
 *
 */
public final class Conditions {

    /**
     * 返回属性 <code>name</code> 取值属于给定集合之一的 IN 条件。
     *
     * @param name 属性名
     * @param values 允许的值数组
     * @return 查询条件
     */
    public static Condition in(String name, Object... values) {
        List<Condition> conditions = new ArrayList<Condition>();
        for (Object value : values) {
            conditions.add(eq(name, value));
        }
        return or(conditions.toArray(new Condition[conditions.size()]));
    }

    /**
     * 返回属性 <code>name</code> 取值属于给定集合之一的 IN 条件。
     *
     * @param name 属性名
     * @param values 允许的值集合
     * @return 查询条件
     */
    public static Condition in(String name, Collection<?> values) {
        List<Condition> conditions = new ArrayList<Condition>();
        for (Object value : values) {
            conditions.add(eq(name, value));
        }
        return or(conditions.toArray(new Condition[conditions.size()]));
    }
    
    /**
     * 返回属性 <code>name</code> 等于 <code>value</code> 的 EQUALS 条件。
     *
     * @param name 属性名
     * @param value 期望值
     * @return 查询条件
     */
    public static Condition eq(String name, Object value) {
        return new EQCondition(name, value);
    }
    
    /**
     * 返回多个嵌套条件的 OR 组合条件。
     *
     * @param conditions 嵌套条件数组
     * @return 查询条件
     */
    public static Condition or(Condition... conditions) {
        return new ORCondition(conditions);
    }

    /**
     * 返回多个嵌套条件的 AND 组合条件。
     *
     * @param conditions 嵌套条件数组
     * @return 查询条件
     */
    public static Condition and(Condition... conditions) {
        return new ANDCondition(conditions);
    }

    /**
     * 返回属性 <code>name</code> 大于 <code>value</code> 的 GT 条件。
     *
     * @param name 属性名
     * @param value 比较值
     * @return 查询条件
     */
    public static Condition gt(String name, Number value) {
        return new GTCondition(name, value);
    }

    /**
     * 返回属性 <code>name</code> 小于 <code>value</code> 的 LT 条件。
     *
     * @param name 属性名
     * @param value 比较值
     * @return 查询条件
     */
    public static Condition lt(String name, Number value) {
        return new LTCondition(name, value);
    }

    /**
     * 返回属性 <code>name</code> 大于等于 <code>value</code> 的 GE 条件。
     *
     * @param name 属性名
     * @param value 比较值
     * @return 查询条件
     */
    public static Condition ge(String name, Number value) {
        return new GECondition(name, value);
    }

    /**
     * 返回属性 <code>name</code> 小于等于 <code>value</code> 的 LE 条件。
     *
     * @param name 属性名
     * @param value 比较值
     * @return 查询条件
     */
    public static Condition le(String name, Number value) {
        return new LECondition(name, value);
    }

    private Conditions() {
    }
    
}
