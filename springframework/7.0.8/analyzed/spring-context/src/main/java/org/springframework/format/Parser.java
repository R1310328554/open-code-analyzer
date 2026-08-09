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

package org.springframework.format;

import java.text.ParseException;
import java.util.Locale;

/**
 * 将文本字符串解析为 T 类型实例。
 *
 * @author Keith Donald
 * @since 3.0
 * @param <T> 本 Parser 所产生的对象类型
 */
@FunctionalInterface
public interface Parser<T> {

	/**
	 * 将文本字符串解析为 T 类型实例。
	 * @param text 文本字符串
	 * @param locale 当前用户区域设置
	 * @return T 类型实例
	 * @throws ParseException 当 java.text 解析库中发生解析异常时
	 * @throws IllegalArgumentException 当发生解析异常时
	 */
	T parse(String text, Locale locale) throws ParseException;

}
