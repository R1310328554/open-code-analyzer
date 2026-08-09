/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jdbc.object;

/**
 * 具体实现，允许在应用上下文中定义 RDBMS 存储过程，
 * 而无需编写自定义 Java 实现类。
 * <p>
 * 本实现不提供类型化的调用方法，执行时必须使用
 * 通用的 {@link StoredProcedure#execute(java.util.Map)} 或
 * {@link StoredProcedure#execute(org.springframework.jdbc.core.ParameterMapper)} 方法。
 *
 * @author Thomas Risberg
 * @see org.springframework.jdbc.object.StoredProcedure
 */
public class GenericStoredProcedure extends StoredProcedure {

}
