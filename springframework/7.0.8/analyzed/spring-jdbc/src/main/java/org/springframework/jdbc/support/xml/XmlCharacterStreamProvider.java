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

import java.io.IOException;
import java.io.Writer;

/**
 * 通过 {@code Writer} 提供 XML 输入数据的回调接口。
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see java.io.Writer
 * @deprecated 自 6.2 起弃用，推荐直接使用 {@link java.sql.SQLXML}
 */
@Deprecated(since = "6.2")
public interface XmlCharacterStreamProvider {

	/**
	 * 实现类须向给定 {@code Writer} 写入 XML 内容。
	 * @param writer 用于提供 XML 输入的 {@code Writer}
	 * @throws IOException 写入 XML 时发生 I/O 错误
	 */
	void provideXml(Writer writer) throws IOException;

}
