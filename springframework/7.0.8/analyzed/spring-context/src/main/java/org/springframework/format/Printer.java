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

import java.util.Locale;

/**
 * 将类型为 T 的对象打印为可显示的文本。
 *
 * @author Keith Donald
 * @since 3.0
 * @param <T> 本 Printer 所打印的对象类型
 */
@FunctionalInterface
public interface Printer<T> {

	/**
	 * 将类型为 T 的对象打印为可显示的文本。
	 * @param object 要打印的实例
	 * @param locale 当前用户区域设置
	 * @return 打印后的文本字符串
	 */
	String print(T object, Locale locale);

}
