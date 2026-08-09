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

package org.springframework.jdbc.support.xml;

import org.springframework.jdbc.support.SqlValue;

/**
 * {@link org.springframework.jdbc.support.SqlValue} 的子接口，专门指示将 XML 数据传递到指定列。
 * @author Thomas Risberg
 * @since 2.5.5
 * @see org.springframework.jdbc.support.SqlValue
 * @deprecated 6.2，支持直接 {@link SqlValue} 实现
 */
@Deprecated(since = "6.2")
public interface SqlXmlValue extends SqlValue {

}
