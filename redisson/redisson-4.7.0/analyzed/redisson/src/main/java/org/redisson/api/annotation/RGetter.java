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
package org.redisson.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记该方法为 Live Object 的通用字段读取器。
 * <p>
 * 被注解方法接受一个 {@link String} 参数（字段名）并返回其值。与已废弃的 {@link RFieldAccessor}
 * 不同，方法名不必为 {@code get}。
 * Example:
 * <pre>
 *       &#064;RGetter
 *       public &lt;T&gt; T get(String field) {
 *           return null;
 *       }
 * </pre>
 *
 * @see RSetter
 *
 * @author Nikita Koksharov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RGetter {
}
