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

import java.util.Collections;

/**
 * 映射回复的空值规范化转换器。
 * <p>
 * Redis 返回 {@code null} 时映射为不可变空映射，便于上层统一迭代。
 *
 * @author Nikita Koksharov
 */
public class EmptyMapConvertor implements Convertor<Object> {

    /** {@code null} 转为 {@link Collections#emptyMap()}，否则原样返回。 */
    @Override
    public Object convert(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        return obj;
    }

}
