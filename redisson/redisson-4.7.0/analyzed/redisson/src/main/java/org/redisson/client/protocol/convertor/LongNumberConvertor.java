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
package org.redisson.client.protocol.convertor;

import java.math.BigDecimal;

/**
 * 将已解码的 {@link Long} 或 {@link Double} 转为调用方期望的数值类型。
 * <p>
 * 构造时指定 {@link #resultClass}，按可赋值关系选择 {@code intValue}、{@link BigDecimal} 等。
 *
 * @author Nikita Koksharov
 *
 */
public class LongNumberConvertor implements Convertor<Object> {

    /** 目标返回类型的 Class 对象。 */
    private Class<?> resultClass;

    /**
     * @param resultClass 期望的数值类型（如 {@link Long}、{@link Integer}、{@link BigDecimal}）
     */
    public LongNumberConvertor(Class<?> resultClass) {
        super();
        this.resultClass = resultClass;
    }

    /** 按 {@link #resultClass} 将 Long/Double 转为对应包装类型或 {@link BigDecimal}。 */
    @Override
    public Object convert(Object result) {
        if (result instanceof Long) {
            Long res = (Long) result;
            if (resultClass.isAssignableFrom(Long.class)) {
                return res;
            }
            if (resultClass.isAssignableFrom(Integer.class)) {
                return res.intValue();
            }
            if (resultClass.isAssignableFrom(BigDecimal.class)) {
                return new BigDecimal(res);
            }
        }
        if (result instanceof Double) {
            Double res = (Double) result;
            if (resultClass.isAssignableFrom(Float.class)) {
                return ((Double) result).floatValue();
            }
            if (resultClass.isAssignableFrom(Double.class)) {
                return res;
            }
        }
        throw new IllegalStateException("Wrong value type!");
    }

}
