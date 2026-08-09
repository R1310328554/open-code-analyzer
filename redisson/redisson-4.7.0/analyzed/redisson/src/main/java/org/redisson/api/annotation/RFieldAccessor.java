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
 * 标记该方法为 Live Object 的通用字段访问器（读/写）。
 * Example:
 * <pre>
 *       &#064;RFieldAccessor
 *       public void set(String field, T value) {
 *       }
 *       
 *       &#064;RFieldAccessor
 *       public Object get(String field) {
 *           return null;
 *       }
 * </pre>
 * 
 * @deprecated 已拆分为更明确的 {@link RGetter} 与 {@link RSetter}，且不再要求方法必须命名为
 *             {@code get}/{@code set}。请改用 {@link RGetter} 标记字段读取、{@link RSetter} 标记字段写入。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RFieldAccessor {}
