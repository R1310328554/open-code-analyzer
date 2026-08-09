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

import javax.xml.transform.Result;

/**
 * 通过 {@code Result} 提供 XML 输入数据的回调接口。
 *
 * @author Thomas Risberg
 * @since 2.5.5
 * @see javax.xml.transform.Result
 * @deprecated 自 6.2 起弃用，推荐直接使用 {@link java.sql.SQLXML}
 */
@Deprecated(since = "6.2")
public interface XmlResultProvider {

	/**
	 * 实现类须向给定 {@code Result} 写入 XML 内容；具体写法取决于所用的 {@code Result} 实现。
	 * @param result 用于提供 XML 输入的 {@code Result}
	 */
	void provideXml(Result result);

}
