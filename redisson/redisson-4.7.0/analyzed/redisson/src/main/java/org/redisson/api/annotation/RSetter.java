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
 * 标记该方法为 Live Object 的通用字段写入器。
 * <p>
 * 被注解方法接受两个参数：字段名（{@link String}）与新值，并将值写入对应字段。与已废弃的
 * {@link RFieldAccessor} 不同，方法名不必为 {@code set}。
 * Example:
 * <pre>
 *       &#064;RSetter
 *       public &lt;T&gt; void set(String field, T value) {
 *       }
 * </pre>
 *
 * @see RGetter
 *
 * @author Nikita Koksharov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RSetter {
}
